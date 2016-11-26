package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when an entity could not be found in storage.
 */
public class MissingEntityException extends CoreException {
    public MissingEntityException(Object key) {
        super(Strings.getMissingEntity(key.toString()));
    }
}
