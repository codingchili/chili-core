package com.codingchili.services.realm.model;

import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 */
public class CharacterExistsException extends Exception {

    public CharacterExistsException(String name) {
        super(Strings.getCharacterExistsError(name));
    }

}
