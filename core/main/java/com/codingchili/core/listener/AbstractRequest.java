package com.codingchili.core.listener;

import com.codingchili.core.security.Token;

/**
 * Base class for requests that parses the token of the request once. Reading a token deserializes
 * it from the request data, which is repeated by every call of {@link Request#token()} on a request
 * that doesn't cache it, and the token of a request is typically read by both the authenticator and the handler.
 */
public abstract class AbstractRequest implements Request {
    private volatile Token token;

    /**
     * Get the request token sent with the request. The token is parsed from the request data
     * the first time that it is read, and the same instance is returned by following calls.
     * Copy the token if it is to be modified, for example before it is signed.
     *
     * @return the requests token
     */
    @Override
    public Token token() {
        Token parsed = token;

        if (parsed == null) {
            parsed = Request.parseToken(data());
            token = parsed;
        }
        return parsed;
    }
}
