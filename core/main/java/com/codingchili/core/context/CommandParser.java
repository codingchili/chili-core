package com.codingchili.core.context;

import java.util.*;

import static com.codingchili.core.configuration.CoreStrings.COMMAND_PREFIX;

/**
 * @author Robin Duda
 *
 * Parses a commandline into a command and properties.
 */
public class CommandParser {
    private String prefix = COMMAND_PREFIX;
    private Map<String, List<String>> properties;
    private String command;

    /**
     * @param commandline a list starting with the command followed by properties.
     *                    example: adduser --name foo --admin
     */
    public CommandParser(String... commandline) {
        parse(commandline);
    }

    /**
     * @param prefix a prefix that prepends the command, removed when parsed.
     *               defaults to COMMAND_PREFIX in
     *               {@link com.codingchili.core.configuration.CoreStrings#COMMAND_PREFIX}
     * @return fluent
     */
    public CommandParser setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    private void reset() {
        properties = new HashMap<>();
        command = null;
    }

    /**
     * Parses the given command line and discards any prior state.
     * @param line a commandline with properties.
     */
    public void parse(String... line) {
        reset();
        String parameter = null;

        if (line.length > 0) {
            command = line[0];
        }

        for (int i = 1; i < line.length; i++) {
            String item = line[i];

            if (item.startsWith(prefix)) {
                parameter = item;
                properties.put(parameter, null);
            } else {
                if (parameter != null) {
                    addOrUpdate(parameter, item);
                } else {
                    // place as property when default parameter.
                    addOrUpdate(line[i - 1], item);
                }
            }
        }
    }

    private void addOrUpdate(String property, String value) {
        properties.computeIfAbsent(property, (key) -> new ArrayList<>());
        properties.computeIfPresent(property, (key, list) -> {
            list.add(value);
            return list;
        });
    }

    /**
     * @return the name of the command that was passed in the commandline.
     */
    public Optional<String> getCommand() {
        if (command == null) {
            return Optional.empty();
        } else {
            return Optional.of(command);
        }
    }

    /**
     * Programmatically adds a property to the parsed command line.
     * @param key the key of the property, example '--name'
     * @param value the value of the property, prefixed with the key.
     */
    public void addProperty(String key, String value) {
        addOrUpdate(key, value);
    }

    /**
     * @param name checks if the parsed commandline specifies the given property
     * @return true if the command contains the property
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * @param name retrieves the property value by its name, excluding the prefix.
     * @return a property value if given, if multiple given then the first is returned.
     */
    public Optional<String> getValue(String name) {
        if (properties.containsKey(name)) {
            List<String> value = properties.get(name);

            if (value != null) {
                return Optional.of(value.get(0));
            }
        }
        return Optional.empty();
    }

    /**
     * @param name the name of the property to check if it has multiple values.
     * @return true if the given property has multiple values.
     */
    public boolean isMulti(String name) {
        if (properties.containsKey(name)) {
            List<String> list = properties.get(name);
            if (list == null) {
                return false;
            } else {
                return properties.get(name).size() > 1;
            }
        } else {
            return false;
        }
    }

    /**
     * @param name retrieves all property values for the given property name, excluding the prefix.
     * @return all property values for the given property name.
     */
    public List<String> getAllValues(String name) {
        return properties.getOrDefault(name, new ArrayList<>());
    }
}
