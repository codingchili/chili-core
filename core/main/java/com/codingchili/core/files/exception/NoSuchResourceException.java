package com.codingchili.core.files.exception;

import com.codingchili.core.context.CoreRuntimeException;

import static com.codingchili.core.configuration.CoreStrings.getNoSuchResource;

/**
 * @author Robin Duda
 * <p>
 * Throw when a resource is missing from the filesystem and the classpath.
 */
public class NoSuchResourceException extends CoreRuntimeException {

    /**
     * @param resource the resource identifier that was not found.
     */
    public NoSuchResourceException(String resource) {
        super(getNoSuchResource(resource));
    }
}
