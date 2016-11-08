package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class RequestPayloadSizeException extends CoreException {
    public RequestPayloadSizeException() {
        super(Strings.ERROR_REQUEST_SIZE_TOO_LARGE);
    }
}
