package com.codingchili.core.files.exception;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Throw when attempting to load a file for an extension no filestore has been registered for.
 */
public class NoFileStoreRegisteredException extends CoreRuntimeException {

    /**
     * @param path      the file that failed to load
     * @param extension the extension that no filestore has been registered for.
     */
    public NoFileStoreRegisteredException(String path, String extension) {
        super(String.format("No filestore registered to handle file '%s' with extension '%s'.", path, extension));
    }
}
