package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class NoSuchCommandException extends CoreException {
    public NoSuchCommandException(String command) {
        super(Strings.getNoSuchCommand(command));
    }
}
