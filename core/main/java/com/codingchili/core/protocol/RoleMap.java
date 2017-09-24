package com.codingchili.core.protocol;

import com.codingchili.core.configuration.CoreStrings;

import java.util.HashMap;
import java.util.Map;

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

    public static void put(String role, RoleType type) {
        roleMap.put(role, type);
    }

    public static RoleType get(String role) {
        if (!roleMap.containsKey(role)) {
            throw new RuntimeException(CoreStrings.getMissingRole(role));
        } else {
            return roleMap.get(role);
        }
    }

    public static RoleType[] get(String[] roles) {
        RoleType[] list = new RoleType[roles.length];
        for (int i = 0; i < roles.length; i++) {
            list[i] = get(roles[i]);
        }
        return list;
    }
}
