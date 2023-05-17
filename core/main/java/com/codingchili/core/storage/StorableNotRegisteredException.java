package com.codingchili.core.storage;

import com.codingchili.core.context.CoreRuntimeException;

public class StorableNotRegisteredException extends CoreRuntimeException {
    public StorableNotRegisteredException(Class<?> type) {
        super("missing registration for %s".formatted(type.getName()));
    }
}
