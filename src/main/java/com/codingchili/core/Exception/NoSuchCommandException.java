package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

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
