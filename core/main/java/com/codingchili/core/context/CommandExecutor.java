package com.codingchili.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.exception.CommandAlreadyExistsException;
import com.codingchili.core.context.exception.NoSuchCommandException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;

import io.vertx.core.Future;

import static com.codingchili.core.configuration.CoreStrings.COMMAND_PREFIX;

/**
 * @author Robin Duda
 *         <p>
 *         Parses and executes commands from the command line.
 */
public class CommandExecutor {
    private static final String UNDEFINED = "undefined";
    protected LauncherSettings settings = Configurations.launcher();
    protected Map<String, Command> commands = new HashMap<>();
    protected Map<String, String> properties = new HashMap<>();
    protected Logger logger = new ConsoleLogger();
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
     * @param future      callback.
     * @param commandLine the commands/properties to execute.
     * @return fluent
     */
    public CommandExecutor execute(Future<Void> future, String... commandLine) {
        parseCommandLine(commandLine);
        Optional<String> command = getCommand();

        if (command.isPresent() && commands.containsKey(command.get())) {
            commands.get(command.get()).execute(future, this);
        } else {
            future.fail(new NoSuchCommandException(getCommand().orElse(UNDEFINED)));
        }
        return this;
    }

    /**
     * Executes the given command synchronously.
     *
     * @param command the command to execute
     * @return fluent
     */
    public CommandExecutor execute(String... command) {
        Future<Void> future = Future.future();
        execute(future, command);

        if (future.failed()) {
            throw new CoreRuntimeException(future.cause().getMessage());
        } else {
            return this;
        }
    }

    private void parseCommandLine(String[] line) {
        String parameter = null;

        if (line.length > 0) {
            command = line[0];
        }

        for (int i = 1; i < line.length; i++) {
            String item = line[i];

            if (item.startsWith(COMMAND_PREFIX)) {
                parameter = item;
                properties.put(parameter, null);
            } else {
                if (parameter != null) {
                    properties.put(parameter, item);
                }
            }
        }
    }

    /**
     * Get the first command passed to the executor.
     *
     * @return the initial command as a string.
     */
    public Optional<String> getCommand() {
        if (command == null) {
            return Optional.empty();
        } else {
            return Optional.of(command);
        }
    }

    /**
     * Adds a new property to the CommandExecutor that is passed to executed commands.
     *
     * @param key   the key to identify the property by
     * @param value a value to bind to the property key
     * @return fluent
     */
    public CommandExecutor addProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Check if a property has been set from the commandline.
     *
     * @param name the name of the property
     * @return true if the property exists
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Get a commandline property passed to the Executor.
     *
     * @param name the name of the property to get
     * @return the property as a string value
     */
    public Optional<String> getProperty(String name) {
        if (properties.containsKey(name)) {
            String value = properties.get(name);

            if (value != null) {
                return Optional.of(properties.get(name));
            }
        }
        return Optional.empty();
    }

    /**
     * Registers a new command to all CommandExecutors.
     *
     * @param command the command to add
     * @return fluent
     */
    public CommandExecutor add(Command command) {
        if (commands.containsKey(command.getName())) {
            throw new CommandAlreadyExistsException(command);
        } else {
            commands.put(command.getName(), command);
        }
        return this;
    }

    /**
     * Adds a new asynchronous command using the default implementation.
     *
     * @param executor    the method to execute when the command is executed.
     * @param name        the name of the command to add
     * @param description the description of the command
     * @return fluent
     */
    public CommandExecutor add(BiFunction<Future<Void>, CommandExecutor, Void> executor, String name, String
            description) {
        return add(new BaseCommand(executor, name, description));
    }

    /**
     * Adds a new synchronous command using the default implementation.
     *
     * @param executor    the method to execute when the command is executed
     * @param name        the name of the command to add
     * @param description the description of the command
     * @return fluent
     */
    public CommandExecutor add(Consumer<CommandExecutor> executor, String name, String description) {
        return add(new BaseCommand(executor, name, description));
    }
}