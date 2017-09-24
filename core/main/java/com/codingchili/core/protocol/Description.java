package com.codingchili.core.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
