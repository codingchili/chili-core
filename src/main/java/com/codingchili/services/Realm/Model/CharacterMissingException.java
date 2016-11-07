package com.codingchili.services.Realm.Model;

import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 *         Thrown when a selected character is missing.
 */
public class CharacterMissingException extends Throwable {
    public CharacterMissingException(String character) {
        super(Strings.getCharacterMissingError(character));
    }
}
