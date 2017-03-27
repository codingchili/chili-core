package com.codingchili.core.context;

import java.util.function.Consumer;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         A basic
 */
public class BaseCommand implements Command {
    private boolean visible = true;
    private Consumer<Future<Void>> command;
    private String name;
    private String description;

    /**
     * Creates a new asynchronous command.
     *
     * @param consumer    the function to be called when the command is executed.
     * @param name        the name of the command
     * @param description the command description
     */
    public BaseCommand(Consumer<Future<Void>> consumer, String name, String description) {
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
    public BaseCommand(Runnable runnable, String name, String description) {
        this(future -> {
            runnable.run();
            future.complete();
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
    public void execute(Future<Void> future) {
        command.accept(future);
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
