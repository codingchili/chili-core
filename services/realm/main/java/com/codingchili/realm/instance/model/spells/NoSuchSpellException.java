package com.codingchili.realm.instance.model.spells;

        import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 *
 * Throw when a spell cannot be found.
 */
public class NoSuchSpellException extends CoreRuntimeException {

    public NoSuchSpellException(String name) {
        super(String.format("No spell was loaded with the name '%s'.", name));
    }
}
