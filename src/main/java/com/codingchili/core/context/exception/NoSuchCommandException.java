package com.codingchili.core.context.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when a command was given on the terminal that does not exist.
 */
public class NoSuchCommandException extends CoreException {
    public NoSuchCommandException(String command) {
        super(Strings.getNoSuchCommand(command));
    }
}
