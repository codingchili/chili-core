package com.codingchili.core.context.exception;

import com.codingchili.core.context.Command;
import com.codingchili.core.context.CoreRuntimeException;

import static com.codingchili.core.configuration.CoreStrings.getCommandAlreadyExistsException;

/**
 * @author Robin Duda
 *         <p>
 *         Thrown when a command that is already registered to the commandexecutor is re-added.
 */
public class CommandAlreadyExistsException extends CoreRuntimeException {

    /**
     * @param command the command that is already registered.
     */
    public CommandAlreadyExistsException(Command command) {
        super(getCommandAlreadyExistsException(command.getName()));
    }

}
