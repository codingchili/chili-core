package com.codingchili.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.exception.CommandAlreadyExistsException;
import com.codingchili.core.context.exception.NoSuchCommandException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         Parses and executes commands from the command line.
 */
public class CommandExecutor {
    protected LauncherSettings settings = Configurations.launcher();
    protected Map<String, Command> commands = new HashMap<>();
    protected Logger logger = new ConsoleLogger();
    private boolean handled = true;
    private String command;

    /**
     * uses a ConsoleLogger as default.
     */
    public CommandExecutor() {
    }

    /**
     * @param logger to write output to.
     */
    public CommandExecutor(Logger logger) {
        this.logger = logger;
    }

    /**
     * Executes the given command. Sets handled to false if the command does not exist.
     *
     * @param future  callback.
     * @param command the command to execute.
     * @return fluent
     */
    public CommandExecutor execute(Future<Void> future, String command) {
        this.command = command;

        if (commands.containsKey(command)) {
            commands.get(command).execute(future);
        } else {
            handled = false;
        }
        return this;
    }

    /**
     * Executes the given command synchronously.
     *
     * @param command the command to execute
     * @return fluent
     */
    public CommandExecutor execute(String command) {
        return execute(Future.future(), command);
    }

    /**
     * Registers a new command to all CommandExecutors.
     *
     * @param command the command to add
     */
    public void add(Command command) {
        if (commands.containsKey(command.getName())) {
            throw new CommandAlreadyExistsException(command);
        } else {
            commands.put(command.getName(), command);
        }
    }

    /**
     * Adds a new asynchronous command using the default implementation.
     *
     * @param executor    the method to execute when the command is executed.
     * @param name        the name of the command to add
     * @param description the description of the command
     */
    public void add(Consumer<Future<Void>> executor, String name, String description) {
        add(new BaseCommand(executor, name, description));
    }

    /**
     * Adds a new synchronous command using the default implementation.
     *
     * @param executor    the method to execute when the command is executed
     * @param name        the name of the command to add
     * @param description the description of the command
     */
    public void add(Runnable executor, String name, String description) {
        add(new BaseCommand(executor, name, description));
    }

    /**
     * @return true if the command exists and was handled successfully.
     */
    public boolean isHandled() {
        return handled;
    }

    /**
     * @return return an exception message.
     */
    public String getError() {
        return new NoSuchCommandException(command).getMessage();
    }
}
