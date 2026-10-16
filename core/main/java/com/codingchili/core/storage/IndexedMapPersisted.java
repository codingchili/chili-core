package com.codingchili.core.storage;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.context.StorageContext;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.disk.DiskIndex;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.resultset.ResultSet;
import io.vertx.core.Promise;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.googlecode.cqengine.query.QueryFactory.equal;

/**
 * Adds disk persistence to IndexedMap.
 * <p>
 * Overrides some methods where the underlying implementation differs between
 * on heap and on disk indexes.
 * <p>
 * The update method for disk persistence cannot be trusted.
 * It only replaces an existing version by using the objects serialized form as its
 * composite PK.
 */
public class IndexedMapPersisted<Value extends Storable> extends IndexedMap<Value> {
    private static final AtomicBoolean LOADED = new AtomicBoolean(false);

    /**
     * Creates a possibly shared instance of the persisted IndexedMap. It is recommended
     * to not call this directly and instead use the storage loader.
     *
     * @param promise  completed when the storage is ready.
     * @param context the storage context to set up file locations etc.
     */
    public IndexedMapPersisted(Promise<AsyncStorage<Value>> promise, StorageContext<Value> context) {
        super((idField) -> {
            synchronized (IndexedMapPersisted.class) {
                LOADED.set(true);

                File file = new File(dbPath(context));
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new RuntimeException("Failed to create dirs for DB " + file.toPath().toAbsolutePath());
                }
                return new ConcurrentIndexedCollection<>(
                        DiskPersistence.onPrimaryKeyInFile(idField, file));
            }
        }, context);

        this.path = dbPath(context);
        heal();
        promise.complete(this);
    }

    private static final String ID_TABLE = "cqtbl_id";

    private final String path;

    /**
     * The id table of the disk persistence has the serialized object as part of its primary key.
     * CQEngine replaces and removes an object by serializing the object that was read from disk.
     * When this does not reproduce the stored bytes (the serialized form changed between versions)
     * the old row is left behind and a second row is added for the same id. Queries on the
     * collection then fail with 'ResultSet contains more than one object'.
     * <p>
     * Removes duplicate rows, keeping the version that is stored in the current serialized form
     * (it was written last) or any version if none is.
     */
    void heal() {
        try (Connection connection = connect()) {
            List<String> duplicated = new ArrayList<>();
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery(
                         "SELECT value FROM " + ID_TABLE + " GROUP BY value HAVING COUNT(*) > 1")) {
                while (rows.next()) {
                    duplicated.add(rows.getString(1));
                }
            }
            for (String id : duplicated) {
                List<Value> versions;
                try (ResultSet<Value> result = db.retrieve(equal(FIELD_ID, id))) {
                    versions = result.stream().toList();
                }
                Value keep = versions.stream().filter(db::contains).findFirst().orElse(versions.get(0));
                // indexes are not registered yet: only the id table is rewritten, index rows are keyed on the id.
                purge(connection, id, false);
                db.add(keep);
                context.logger(getClass()).log(String.format(
                        "removed %d stale versions of '%s' in %s", versions.size() - 1, id, path));
            }
        } catch (SQLException e) {
            // never prevent the storage from loading.
            context.logger(getClass()).onError(e);
        }
    }

    @Override
    protected boolean replace(Value current, Value replacement) {
        try (Connection connection = connect()) {
            Set<ByteBuffer> before = rows(connection, replacement.getId());
            boolean replaced = super.replace(current, replacement);
            if (replaced) {
                Set<ByteBuffer> added = rows(connection, replacement.getId());
                added.removeAll(before);
                // the old row was not found by its re-serialized form, only keep the new version.
                if (added.size() == 1) {
                    try (var statement = connection.prepareStatement(
                            "DELETE FROM " + ID_TABLE + " WHERE value = ? AND objectKey <> ?")) {
                        statement.setString(1, replacement.getId());
                        statement.setBytes(2, added.iterator().next().array());
                        statement.executeUpdate();
                    }
                }
            }
            return replaced;
        } catch (SQLException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @Override
    protected boolean discard(Value entry) {
        super.discard(entry);
        try (Connection connection = connect()) {
            // rows that could not be matched by their re-serialized form are left behind.
            purge(connection, entry.getId(), true);
            return true;
        } catch (SQLException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    private static Set<ByteBuffer> rows(Connection connection, String id) throws SQLException {
        Set<ByteBuffer> rows = new HashSet<>();
        try (var statement = connection.prepareStatement(
                "SELECT objectKey FROM " + ID_TABLE + " WHERE value = ?")) {
            statement.setString(1, id);
            try (var result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(ByteBuffer.wrap(result.getBytes(1)));
                }
            }
        }
        return rows;
    }

    private static void purge(Connection connection, String id, boolean indexes) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (var statement = connection.createStatement();
             var result = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = 'table' AND name LIKE 'cqtbl\\_%' ESCAPE '\\'")) {
            while (result.next()) {
                tables.add(result.getString(1));
            }
        }
        for (String table : tables) {
            if (!indexes && !table.equals(ID_TABLE)) {
                continue;
            }
            // the id table is keyed on the serialized object, the indexes on the id.
            String column = (table.equals(ID_TABLE)) ? "value" : "objectKey";
            try (var statement = connection.prepareStatement("DELETE FROM " + table + " WHERE " + column + " = ?")) {
                statement.setString(1, id);
                statement.executeUpdate();
            }
        }
    }

    private static String dbPath(StorageContext ctx) {
        return String.format("%s/%s.sqlite", ctx.database(), ctx.collection());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addIndexesForAttribute(Attribute<Value, ?> attribute) {
        if (Comparable.class.isAssignableFrom(attribute.getAttributeType())) {
            db.addIndex(DiskIndex.onAttribute((Attribute<Value, Comparable>) attribute));
        }
    }

    /**
     * Disk indexes are not discovered by CQEngine at startup. This means that CQEngine
     * will not update existing indexes unless AsyncStorage#addIndex is called before any
     * items are added.
     * <p>
     * To work around this there are two options,
     * <p>
     * a) Always add all indexes before inserting ANY items.
     * b) Call this method before loading the IndexedMapPersisted plugin. Then items
     * can be added before adding an index. Because when the index is added later it
     * will be rebuilt. In the window between adding the object and adding the index
     * the object will not be visible in the affected indexes.
     * <p>
     * If this method is called after an instance of IndexedMapPersisted has been loaded
     * through the storage loader an exception is thrown.
     */
    public static void reindex() {
        if (LOADED.get()) {
            throw new CoreRuntimeException(CoreStrings.ERROR_ALREADY_INITIALIZED);
        } else {
            System.setProperty("cqengine.reinit.preexisting.indexes", Boolean.TRUE.toString());
        }
    }
}
