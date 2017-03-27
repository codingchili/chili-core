package com.codingchili.core.context;


import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         A command that may be executed by the CommandExecutor.
 */
public interface Command {

    /**
     * Indicates if this command should be printed in the help dialog.
     *
     * @return true if the command should be listed in the help menu.
     */
    boolean isVisible();

    /**
     * Executes a command.
     */
    void execute(Future<Void> future);

    /**
     * @return the command description.
     */
    String getDescription();

    /**
     * @return the name of the command.
     */
    String getName();
}
