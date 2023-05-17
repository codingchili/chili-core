package com.codingchili.core.storage;

import com.codingchili.core.context.CoreRuntimeException;

public class AttributeNotRegisteredException extends CoreRuntimeException {
    public AttributeNotRegisteredException(Class<?> type, String fieldName) {
        super("attribute '%s' not registered for %s"
                .formatted(fieldName, type.getName()));
    }
}
