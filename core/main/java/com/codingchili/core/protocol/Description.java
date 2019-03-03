package com.codingchili.core.protocol;

import java.lang.annotation.*;

/**
 * Annotation that can add a description to handlers and
 * routes in a handler. Retrieved when using the protocol
 * documentation feature.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Description {
    String value() default "No description available.";
}
