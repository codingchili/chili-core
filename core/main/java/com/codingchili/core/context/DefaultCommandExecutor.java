package com.codingchili.core.context;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.context.exception.CommandAlreadyExistsException;
import com.codingchili.core.context.exception.NoSuchCommandException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import io.vertx.core.Future;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.codingchili.core.configuration.CoreStrings.COMMAND_PREFIX;

/**
 * @author Robin Duda
 * <p>
 * Parses and executes commands from the command line.
 */
public class DefaultCommandExecutor implements CommandExecutor {
    private static final String UNDEFINED = "undefined";
    protected LauncherSettings settings = Configurations.launcher();
    protected Map<String, Command> commands = new HashMap<>();
    protected Map<String, String> properties = new HashMap<>();
    protected Logger logger = new ConsoleLogger(getClass());
    private String command;

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
    public CommandExecutor execute(Future<Boolean> future, String... commandLine) {
        parseCommandLine(commandLine);
        Optional<String> command = getCommand();

        if (command.isPresent() && commands.containsKey(command.get())) {
            Future<Boolean> execution = Future.future();
            commands.get(command.get()).execute(execution, this);
            execution.setHandler(future);
        } else {
            future.fail(new NoSuchCommandException(getCommand().orElse(UNDEFINED)));
        }
        return this;
    }

    @Override
    public Boolean execute(String... command) {
        Future<Boolean> future = Future.future();
        execute(future, command);

        if (future.failed()) {
            throw new CoreRuntimeException(future.cause().getMessage());
        } else {
            return future.result();
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

    @Override
    public Optional<String> getCommand() {
        if (command == null) {
            return Optional.empty();
        } else {
            return Optional.of(command);
        }
    }

    @Override
    public CommandExecutor addProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public Optional<String> getProperty(String name) {
        if (properties.containsKey(name)) {
            String value = properties.get(name);

            if (value != null) {
                return Optional.of(properties.get(name));
            }
        }
        return Optional.empty();
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
    public CommandExecutor add(BiFunction<Future<Boolean>, CommandExecutor, Void> executor, String name, String
            description) {
        return add(new BaseCommand(executor, name, description));
    }

    @Override
    public CommandExecutor add(Function<CommandExecutor, Boolean> executor, String name, String description) {
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