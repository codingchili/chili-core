package com.codingchili.core.context;

/**
 * @author Robin Duda
 *
 * A basic
 */
public class BaseCommand implements Command {
    private boolean visible = true;
    private Runnable command;
    private String name;
    private String description;

    /**
     * Creates a new command.
     *
     * @param function    the function to be called when the command is executed.
     * @param name        the name of the command
     * @param description the command description
     */
    public BaseCommand(Runnable function, String name, String description) {
        this.command = function;
        this.name = name;
        this.description = description;
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
    public void execute() {
        command.run();
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
