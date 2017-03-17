package com.codingchili.core.storage.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * Throw when a reusable query has an invalid format.
 */
public class QueryFormatException extends CoreRuntimeException {
    /**
     * @param query the serialized query as a string.
     */
    public QueryFormatException(String query) {
        super(CoreStrings.getInvalidQueryFormat(query));
    }
}
