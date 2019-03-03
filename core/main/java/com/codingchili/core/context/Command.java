package com.codingchili.core.context;


import io.vertx.core.Future;

/**
 * A command that may be executed by the CommandExecutor.
 */
public interface Command {

    /**
     * Indicates if this command should be printed in the help dialog.
     *
     * @return true if the command should be listed in the help menu.
     */
    default boolean isVisible() {
        return true;
    }

    /**
     * Executes a command.
     *
     * @param future   callback: complete with true to abort startup.
     * @param executor the executor executing the command, can be used to get properties.
     */
    void execute(Future<CommandResult> future, CommandExecutor executor);

    /**
     * @return the command description.
     */
    String getDescription();

    /**
     * @return the name of the command.
     */
    String getName();
}
