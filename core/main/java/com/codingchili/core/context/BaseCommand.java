package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A basic
 */
public class BaseCommand implements Command {
    private BiFunction<Promise<CommandResult>, CommandExecutor, Void> command;
    private boolean visible = true;
    private String name;
    private String description;

    /**
     * Creates a new asynchronous command.
     *
     * @param consumer    the function to be called when the command is executed.
     * @param name        the handler of the command
     * @param description the command description
     */
    public BaseCommand(BiFunction<Promise<CommandResult>, CommandExecutor, Void> consumer, String name, String description) {
        this.command = consumer;
        this.name = name;
        this.description = description;
    }

    /**
     * Creates a  new synchronous command
     *
     * @param runnable    executed when the command is invoked
     * @param name        the handler of the command
     * @param description the command description
     */
    public BaseCommand(Function<CommandExecutor, CommandResult> runnable, String name, String description) {
        this((future, executor) -> {
            future.complete(runnable.apply(executor));
            return null;
        }, name, description);
    }

    public Command setVisible(Boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void execute(Promise<CommandResult> future, CommandExecutor executor) {
        command.apply(future, executor);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return name;
    }
}
