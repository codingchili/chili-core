package com.codingchili.realm.instance.model.spells;

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
 * Container of all registered spells.
 */
public class SpellDB {
    public static final String CONF_PATH = "conf/game/spells/";
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static Map<String, Spell> spells = new HashMap<>();
    private Logger logger;

    /**
     * @param game the context to run the DB on.
     */
    public SpellDB(GameContext game) {
        this.logger = game.getLogger(getClass());

        if (!initialized.getAndSet(true)) {
            spells = ConfigurationFactory.readDirectory(CONF_PATH).stream()
                    .map(config -> Serializer.unpack(config, Spell.class))
                    .collect(Collectors.toMap((k) -> k.name, (v) -> v));

            FileWatcher.builder(game.getInstance())
                    .onDirectory(CONF_PATH)
                    .rate(() -> 1000)
                    .withListener(new FileStoreListener() {
                        @Override
                        public void onFileModify(Path path) {
                            logger.log("spell updated " + path.toString());
                            Spell spell = Serializer.unpack(
                                    ConfigurationFactory.readObject(path.toString()), Spell.class);
                            spells.put(spell.getName(), spell);
                        }
                    })
                    .build();
        }
    }

    /**
     * @param name the name/id of the spell to retrieve.
     * @return the spell if found, throws an exception if not found.
     */
    public Spell getByName(String name) {
        Spell spell = spells.get(name);

        if (spell == null) {
            throw new NoSuchSpellException(name);
        }
        return spell;
    }

    /**
     * @return a list of all available spells.
     */
    public Collection<Spell> list() {
        return spells.values();
    }
}
