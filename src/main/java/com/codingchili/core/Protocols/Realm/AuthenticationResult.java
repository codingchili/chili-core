package com.codingchili.core.Protocols.Realm;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Header;

/**
 * @author Robin Duda
 *         Contains the result of an authentication attempt.
 */
public class AuthenticationResult {
    private static final String ACTION = Strings.REALM_AUTHENTICATION_RESULT;
    private boolean success;
    private Header header;

    public AuthenticationResult() {
    }

    public AuthenticationResult(boolean result) {
        this.success = result;
        this.header = new Header(ACTION);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
