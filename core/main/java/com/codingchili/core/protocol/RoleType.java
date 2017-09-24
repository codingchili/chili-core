package com.codingchili.core.protocol;

/**
 * Identifies a role with a name and an access level.
 */
public interface RoleType {

    /**
     * Returns the name of the role, must be unique.
     *
     * @return the name fo the role.
     */
    String getName();

    /**
     * Roles level of the role, a role with a higher access level
     * gains the privilegies of all roles with a lesser level.
     * <p>
     * To disable this feature set all levels to 0.
     *
     * @return the access level of the role.
     */
    int getLevel();
}
