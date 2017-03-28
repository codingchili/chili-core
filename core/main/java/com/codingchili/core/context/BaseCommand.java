package com.codingchili.core.context;

import java.util.function.*;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         A basic
 */
public class BaseCommand implements Command {
    private BiFunction<Future<Void>, CommandExecutor, Void> command;
    private boolean visible = true;
    private String name;
    private String description;

    /**
     * Creates a new asynchronous command.
     *
     * @param consumer    the function to be called when the command is executed.
     * @param name        the name of the command
     * @param description the command description
     */
    public BaseCommand(BiFunction<Future<Void>, CommandExecutor, Void> consumer, String name, String description) {
        this.command = consumer;
        this.name = name;
        this.description = description;
    }

    /**
     * Creates a  new synchronous command
     *
     * @param runnable    executed when the command is invoked
     * @param name        the name of the command
     * @param description the command description
     */
    public BaseCommand(Consumer<CommandExecutor> runnable, String name, String description) {
        this((future, executor) -> {
            runnable.accept(executor);
            future.complete();
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
    public void execute(Future<Void> future, CommandExecutor executor) {
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
