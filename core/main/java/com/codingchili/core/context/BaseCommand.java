package com.codingchili.core.context;

import io.vertx.core.Future;

import java.util.function.Function;

/**
 * A basic
 */
public class BaseCommand implements Command {
    private Function<CommandExecutor, Future<CommandResult>> command;
    private boolean visible = true;
    private String name;
    private String description;

    private BaseCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Creates a new synchronous command, exceptions thrown by the command fail the result.
     *
     * @param runnable    executed when the command is invoked
     * @param name        the handler of the command
     * @param description the command description
     */
    public BaseCommand(Function<CommandExecutor, CommandResult> runnable, String name, String description) {
        this(name, description);
        this.command = executor -> {
            try {
                return Future.succeededFuture(runnable.apply(executor));
            } catch (Throwable e) {
                return Future.failedFuture(e);
            }
        };
    }

    /**
     * Creates a new asynchronous command.
     *
     * @param command     the function to be called when the command is executed.
     * @param name        the handler of the command
     * @param description the command description
     * @return a new command.
     */
    public static BaseCommand async(Function<CommandExecutor, Future<CommandResult>> command, String name,
                                    String description) {
        BaseCommand base = new BaseCommand(name, description);
        base.command = command;
        return base;
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
    public Future<CommandResult> execute(CommandExecutor executor) {
        return command.apply(executor);
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
