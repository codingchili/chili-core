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

    /**
     * @param name  the name of the role to create.
     * @param level the access level of the role.
     */
    Role(String name, int level) {
        this.name = name;
        this.level = level;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
