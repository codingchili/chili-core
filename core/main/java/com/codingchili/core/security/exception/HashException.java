package com.codingchili.core.security.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * Throw when a hash comparison has failed.
 */
public class HashException extends CoreException {
    public HashException() {
        super(CoreStrings.getHashingFailed());
    }
}
