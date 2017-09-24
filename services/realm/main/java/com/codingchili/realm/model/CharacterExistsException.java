package com.codingchili.realm.model;

import com.codingchili.common.Strings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Throw when an attempt to create a character that already exists occurs.
 */
public class CharacterExistsException extends CoreException {

    public CharacterExistsException(String name) {
        super(Strings.getCharacterExistsError(name), ResponseStatus.CONFLICT);
    }

}
