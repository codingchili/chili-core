package com.codingchili.core.protocol;

/**
 * Extend this interface to provide more role types.
 * These values are used in annotations.
 */
public enum Role implements RoleType {
    ADMIN("admin", 100),
    USER("user", 25),
    PUBLIC("public", 0),
    UNSET("unset", -1);

    private String name;
    private int level;

    Role(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}
