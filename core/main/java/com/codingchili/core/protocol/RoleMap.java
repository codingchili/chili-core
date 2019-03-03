package com.codingchili.core.protocol;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.configuration.CoreStrings;

/**
 * Add custom roles to the rolemap, must be done before initializing protocols.
 * By mapping the name of the role to an instance annotations are no longer
 * limited to the default roles in #{@link Role}.
 */
public class RoleMap {
    public static final String ADMIN = "admin";
    public static final String USER = "user";
    public static final String PUBLIC = "public";
    public static final String UNSET = "unset";
    private static Map<String, RoleType> roleMap = new HashMap<>();

    static {
        put(ADMIN, Role.ADMIN);
        put(USER, Role.USER);
        put(PUBLIC, Role.PUBLIC);
    }

    /**
     * Adds a role mapping.
     *
     * @param name the name of the role.
     * @param role the role implementation.
     */
    public static void put(String name, RoleType role) {
        roleMap.put(name, role);
    }

    /**
     * retrieves a role by its name.
     *
     * @param name the name of the role to retrieve.
     * @return a registered role that matches the given name, throws an
     * exception if the role is not registered.
     */
    public static RoleType get(String name) {
        if (!roleMap.containsKey(name)) {
            throw new RuntimeException(CoreStrings.getMissingRole(name));
        } else {
            return roleMap.get(name);
        }
    }

    /**
     * @param names multiple names to retrieve the roles for.
     * @return a list of roles that matches the given list.
     * Throws an exception if any of the roles in the given names
     * is not registered.
     */
    public static RoleType[] get(String... names) {
        RoleType[] list = new RoleType[names.length];
        for (int i = 0; i < names.length; i++) {
            list[i] = get(names[i]);
        }
        return list;
    }
}
