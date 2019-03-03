package com.codingchili.core.protocol;

import java.lang.annotation.*;

/**
 * Indicates that the annotated method does not require authentication.
 * <p>
 * If set on a Handler, defaults all routes that are missing
 * a value for the role to the given role.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Api {

    /**
     * @return the role that may access the route.
     * if unset, will inherit the role from handlers @Roles annotation.
     */
    String[] value() default {RoleMap.UNSET};

    /**
     * @return overrides the method name as the route name.
     */
    String route() default "";
}
