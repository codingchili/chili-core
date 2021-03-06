package com.codingchili.core.context;

import io.vertx.core.Promise;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.exception.CommandAlreadyExistsException;
import com.codingchili.core.context.exception.NoSuchCommandException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;

/**
 * Parses and executes commands from the command line.
 */
public class DefaultCommandExecutor implements CommandExecutor {
    protected LauncherSettings settings = Configurations.launcher();
    protected Map<String, Command> commands = new HashMap<>();
    protected Logger logger = new StringLogger(getClass());
    private CommandParser parser = new CommandParser();

    /**
     * uses a ConsoleLogger as default.
     */
    public DefaultCommandExecutor() {
    }

    /**
     * @param logger to write output to.
     */
    public DefaultCommandExecutor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public CommandExecutor execute(Promise<CommandResult> future, String... commandLine) {
        parser.parse(commandLine);
        Optional<String> command = getCommand();

        if (command.isPresent() && commands.containsKey(command.get())) {
            Promise<CommandResult> execution = Promise.promise();
            commands.get(command.get()).execute(execution,  this);
            execution.future().onComplete(future);
        } else {
            future.fail(new NoSuchCommandException(getCommand().orElse("")));
        }
        return this;
    }

    @Override
    public CommandResult execute(String... command) {
        Promise<CommandResult> promise = Promise.promise();
        execute(promise, command);

        if (promise.future().failed()) {
            throw new CoreRuntimeException(promise.future().cause().getMessage());
        } else {
            return promise.future().result();
        }
    }

    @Override
    public Optional<String> getCommand() {
        return parser.getCommand();
    }

    public CommandParser getParser() {
        return parser;
    }

    @Override
    public CommandExecutor addProperty(String key, String value) {
        parser.addProperty(key, value);
        return this;
    }

    @Override
    public boolean hasProperty(String name) {
        return parser.hasProperty(name);
    }

    @Override
    public Optional<String> getProperty(String name) {
        return parser.getValue(name);
    }

    @Override
    public List<String> getAllProperties(String name) {
        return parser.getAllValues(name);
    }

    @Override
    public CommandExecutor add(Command command) {
        if (commands.containsKey(command.getName())) {
            throw new CommandAlreadyExistsException(command);
        } else {
            commands.put(command.getName(), command);
        }
        return this;
    }

    @Override
    public CommandExecutor add(BiFunction<Promise<CommandResult>, CommandExecutor, Void> executor, String name, String
            description) {
        return add(new BaseCommand(executor, name, description));
    }

    @Override
    public CommandExecutor add(Function<CommandExecutor, CommandResult> executor, String name, String description) {
        return add(new BaseCommand(executor, name, description));
    }

    @Override
    public Collection<Command> list() {
        return commands.values();
    }

    @Override
    public CommandExecutor clear() {
        commands.clear();
        return this;
    }
}