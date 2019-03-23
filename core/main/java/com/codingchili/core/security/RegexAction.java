package com.codingchili.core.security;

/**
 * Determines if text matching a regex should be replaced or if the
 * validation should fail.
 */
public enum RegexAction {

    /**
     * Rejects the input whenever the configured regex matches.
     */
    REJECT,

    /**
     * Substitutes parts of the input which matches the regex.
     */
    SUBSTITUTE,

    /**
     * Accepts the input if there is a complete match for the input.
     */
    ACCEPT
}