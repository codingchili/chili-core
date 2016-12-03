package com.codingchili.realm.model;

import com.codingchili.common.Strings;

/**
 * @author Robin Duda
 */
public class CharacterExistsException extends Exception {

    public CharacterExistsException(String name) {
        super(Strings.getCharacterExistsError(name));
    }

}
