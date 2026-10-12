package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.CoreStrings;

public class MissingKeyStoreException extends RuntimeException {
    public MissingKeyStoreException(String alias) {
        super(CoreStrings.getMissingKeyStore(alias));
    }
}
