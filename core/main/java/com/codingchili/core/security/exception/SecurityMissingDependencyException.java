package com.codingchili.core.security.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * Created by robdu on 2017-03-10.
 */
public class SecurityMissingDependencyException extends CoreException {
    public SecurityMissingDependencyException(String target, String identifier) {
        super(CoreStrings.getSecurityDependencyMissing(target, identifier));
    }
}
