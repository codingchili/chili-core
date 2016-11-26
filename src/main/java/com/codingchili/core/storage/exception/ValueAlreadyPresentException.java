package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when attempting to put-if-absent but value is not absent.
 */
public class ValueAlreadyPresentException extends CoreException {
    public ValueAlreadyPresentException(Object key) {
        super(Strings.getValueAlreadyPresent(key.toString()));
    }
}
