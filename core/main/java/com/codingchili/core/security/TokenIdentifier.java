package com.codingchili.core.security;

/**
 * Declares a security dependency on another service. Using a token identifier it is possible to
 * generate a token that is signed with the secret of the specified service. The secret that is used
 * here should match the declared secret on the service object. This is a name and not the actual value
 * of the secret, this will be generated.
 *
 * Used with the {@link AuthenticationGenerator}.
 */
public class TokenIdentifier {
    private String service;
    private String secret;

    /**
     * Create a new empty identifier.
     */
    public TokenIdentifier() {
    }

    /**
     * Creates a new identifier with the target service and secret name set.
     * @param service the name of the service a token should be generated for.
     * @param secret the name of the service to use when signing the token.
     */
    public TokenIdentifier(String service, String secret) {
        this.service = service;
        this.secret = secret;
    }

    /**
     * @return the name of the service that owns the secret used for signing the token.
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the name of the service that owns the secret used for signing the token.
     * @return fluent
     */
    public TokenIdentifier setService(String service) {
        this.service = service;
        return this;
    }

    /**
     * @return the name of the secret from the target service to use to sign the token.
     */
    public String getSecret() {
        return secret;
    }

    /**
     * @param secret the name of the secret from the target service to use to sign the token.
     * @return fluent
     */
    public TokenIdentifier setSecret(String secret) {
        this.secret = secret;
        return this;
    }
}
