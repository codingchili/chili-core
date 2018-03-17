package com.codingchili.realm.instance.model.afflictions;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Throw when an affliction cannot be found.
 */
public class NoSuchAfflictionException extends CoreRuntimeException {

    public NoSuchAfflictionException(String name) {
        super(String.format("No affliction with name '%s' loaded.", name));
    }
}
