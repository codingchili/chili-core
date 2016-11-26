package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when the replace operation cannot complete as there is nothing to replace.
 */
public class NothingToReplaceException extends CoreException {

    public NothingToReplaceException(Object key) {
        super(Strings.getNothingToReplaceException(key.toString()));
    }
}
