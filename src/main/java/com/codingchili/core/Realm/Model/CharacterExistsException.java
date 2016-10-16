package com.codingchili.core.Realm.Model;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class CharacterExistsException extends Exception {

    public CharacterExistsException(String name) {
        super(Strings.getCharacterExistsError(name));
    }

}
