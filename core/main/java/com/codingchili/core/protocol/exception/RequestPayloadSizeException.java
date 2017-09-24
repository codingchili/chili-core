package com.codingchili.core.protocol.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Throw when a request with too large payload has been received.
 */
public class RequestPayloadSizeException extends CoreException {

    /**
     * @param maxRequestBytes the maximum number of bytes.
     */
    public RequestPayloadSizeException(int maxRequestBytes) {
        super(CoreStrings.getRequestTooLarge(maxRequestBytes), ResponseStatus.BAD);
    }
}
