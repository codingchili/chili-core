package com.codingchili.core.protocol.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when a request with too large payload has been received.
 */
public class RequestPayloadSizeException extends CoreException {
    public RequestPayloadSizeException() {
        super(CoreStrings.ERROR_REQUEST_SIZE_TOO_LARGE);
    }
}
