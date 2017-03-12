package com.codingchili.core.security.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * Throw when a security configuration is pointing out a secret as a token dependency
 * that does not exist in the targeted service.
 */
public class SecurityMissingDependencyException extends CoreRuntimeException {

    /**
     * @param target the name of the service that a token-secret was requested for.
     * @param identifier the name of the secret that was requested.
     */
    public SecurityMissingDependencyException(String target, String identifier) {
        super(CoreStrings.getSecurityDependencyMissing(target, identifier));
    }
}
