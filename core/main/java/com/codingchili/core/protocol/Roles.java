package com.codingchili.core.protocol;

import java.lang.annotation.*;

/**
 * Sets the default access level on a handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Roles {
    /**
     * @return default role required to access handler.
     */
    String[] value() default {RoleMap.USER};
}
