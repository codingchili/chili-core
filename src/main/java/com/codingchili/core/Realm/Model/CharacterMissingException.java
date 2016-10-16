package com.codingchili.core.Realm.Model;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *         Thrown when a selected character is missing.
 */
class CharacterMissingException extends Throwable {
    public CharacterMissingException(String character) {
        super(Strings.getCharacterMissingError(character));
    }
}
