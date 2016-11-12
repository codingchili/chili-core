package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Throw when a request with too large payload has been received.
 */
public class RequestPayloadSizeException extends CoreException {
    public RequestPayloadSizeException() {
        super(Strings.ERROR_REQUEST_SIZE_TOO_LARGE);
    }
}
