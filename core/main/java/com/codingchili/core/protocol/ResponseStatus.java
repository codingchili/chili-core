package com.codingchili.core.protocol;

/**
 * List of the available response statuses, each mapped to the HTTP status code
 * used by transports that support status codes (REST).
 */
public enum ResponseStatus {
    UNAUTHORIZED(401), MISSING(404), CONFLICT(409), ACCEPTED(202), BAD(400), ERROR(500);

    private final int code;

    ResponseStatus(int code) {
        this.code = code;
    }

    /**
     * @return the HTTP status code that corresponds to this status.
     */
    public int code() {
        return code;
    }
}
