package com.codingchili.core.context.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when a command was given on the terminal that does not exist.
 */
public class NoSuchCommandException extends CoreException {

    /**
     * @param command the requested command that was missing.
     */
    public NoSuchCommandException(String command) {
        super(CoreStrings.getNoSuchCommand(command));
    }
}
