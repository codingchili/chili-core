package com.codingchili.core.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to sed the data transfer object for a protocol,
 * used for documentation purposes.
 *
 * If set on a class sets the model of the whole handler,
 * does not affect routes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataModel {
    Class<?> value();
}
