package com.codingchili.realm.model;

import com.codingchili.common.Strings;

/**
 * @author Robin Duda
 * Thrown when a selected character is missing.
 */
public class CharacterMissingException extends Throwable {
    public CharacterMissingException(String character) {
        super(Strings.getCharacterMissingError(character));
    }
}
