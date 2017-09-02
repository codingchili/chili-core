package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when attempting to put-if-absent but value is not absent.
 */
public class ValueAlreadyPresentException extends CoreException {
    public ValueAlreadyPresentException(Object key) {
        super(CoreStrings.getValueAlreadyPresent(key.toString()), ResponseStatus.CONFLICT);
    }
}
