package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
class RequestAttributeMissingException extends CoreException {
    RequestAttributeMissingException() {
        super(Strings.ERROR_PROTOCOL_ATTRIBUTE_MISSING);
    }
}
