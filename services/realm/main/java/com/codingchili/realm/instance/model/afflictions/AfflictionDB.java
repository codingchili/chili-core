package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.realm.instance.context.GameContext;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.codingchili.core.files.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * Container of all afflictions.
 */
public class AfflictionDB {
    public static final String CONF_PATH = "conf/game/afflictions";
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static Map<String, Affliction> afflictions = new HashMap<>();
    private Logger logger;

    /**
     * @param game the game context that is associated with this instance.
     */
    public AfflictionDB(GameContext game) {
        this.logger = game.getLogger(getClass());

        if (!initialized.getAndSet(true)) {
            afflictions = ConfigurationFactory.readDirectory(CONF_PATH).stream()
                    .map(config -> Serializer.unpack(config, Affliction.class))
                    .filter(Objects::nonNull)
                    .filter(affliction -> affliction.name != null)
                    .collect(Collectors.toMap((k) -> k.name, (v) -> v));

            FileWatcher.builder(game.getInstance())
                    .onDirectory(CONF_PATH)
                    .rate(() -> 1000)
                    .withListener(new FileStoreListener() {
                        @Override
                        public void onFileModify(Path path) {
                            logger.log("affliction updated: " + path.toString());
                            Affliction affliction = Serializer.unpack(
                                    ConfigurationFactory.readObject(path.toString()), Affliction.class);

                            afflictions.put(affliction.getName(), affliction);
                        }
                    });
        }
    }

    /**
     * @param name the name of the affliction to find.
     * @return the affliction matching the given name.
     */
    public Affliction getByName(String name) {
        Affliction affliction = afflictions.get(name);

        if (affliction == null) {
            throw new NoSuchAfflictionException(name);
        }

        return affliction;
    }
}
