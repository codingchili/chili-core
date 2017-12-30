package com.codingchili.realm.instance.model.spells;

import com.codingchili.realm.instance.context.GameContext;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.files.*;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * Container of all registered spells.
 */
public class SpellDB {
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static Map<String, Spell> spells = new HashMap<>();

    /**
     * @param game the context to run the DB on.
     */
    public SpellDB(GameContext game) {
        if (!initialized.getAndSet(true)) {
            FileWatcher.builder(game.getInstance())
                    .onDirectory("conf/game/spells/")
                    .rate(() -> 1000)
                    .withListener(new FileStoreListener() {
                        @Override
                        public void onFileModify(Path path) {
                            System.out.println("spell modified " + path.toString());
                            Spell spell = Serializer.unpack(ConfigurationFactory.readObject(path.toString()), Spell.class);
                            spells.put(spell.getId(), spell);
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
            throw new NoSuchSpellException(String.format("No spell was loaded with the name '%s'.", name));
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
