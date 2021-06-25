package com.codingchili.core.context;

import io.vertx.core.Promise;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Interface that can be implemented to handle commandline arguments.
 * Used by #{@link LaunchContext}.
 */
public interface CommandExecutor {
    /**
     * Executes the given command. Sets handled to false if the command does not exist.
     *
     * @param future      callback: true if startup should be aborted.
     * @param commandLine the commands/properties to execute.
     * @return fluent
     */
    CommandExecutor execute(Promise<CommandResult> future, String... commandLine);

    /**
     * Executes the given command synchronously.
     *
     * @param command the command to execute
     * @return true if startup is to be aborted.
     */
    CommandResult execute(String... command);

    /**
     * Get the first command passed to the executor.
     *
     * @return the initial command as a string.
     */
    Optional<String> getCommand();

    /**
     * Adds a new property to the CommandExecutor that is passed to executed commands.
     *
     * @param key   the key to identify the property by
     * @param value a value to bind to the property key
     * @return fluent
     */
    CommandExecutor addProperty(String key, String value);

    /**
     * Check if a property has been set from the commandline.
     *
     * @param name the name of the property
     * @return true if the property exists
     */
    boolean hasProperty(String name);

    /**
     * Get a commandline property passed to the Executor.
     *
     * @param name the name of the property to get
     * @return the property as a string value
     */
    Optional<String> getProperty(String name);

    /**
     * Get a commandline property passed to the Executor.
     * @param name the name of the property to get
     * @return a list of all the property values.
     */
    List<String> getAllProperties(String name);

    /**
     * Registers a new command to the CommandExecutor.
     *
     * @param command the command to add
     * @return fluent
     */
    CommandExecutor add(Command command);

    /**
     * Adds a new asynchronous command using the default implementation.
     *
     * @param executor    the method to execute when the command is executed.
     * @param name        the name of the command to add
     * @param description the description of the command
     * @return fluent
     */
    CommandExecutor add(BiFunction<Promise<CommandResult>, CommandExecutor, Void> executor, String name, String
            description);

    /**
     * Adds a new synchronous command using the default implementation.
     *
     * @param executor    the method to execute when the command is executed
     * @param name        the name of the command to add
     * @param description the description of the command
     * @return fluent
     */
    CommandExecutor add(Function<CommandExecutor, CommandResult> executor, String name, String description);

    /**
     * Lists all commands added to the executor.
     *
     * @return a list of commands registered.
     */
    Collection<Command> list();

    /**
     * Removes all registered commands.
     *
     * @return fluent.
     */
    CommandExecutor clear();
}
