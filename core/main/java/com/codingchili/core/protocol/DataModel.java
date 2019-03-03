package com.codingchili.core.protocol;

import java.lang.annotation.*;

/**
 * Used to sed the data transfer object for a protocol,
 * used for documentation purposes.
 * <p>
 * If set on a class sets the model of the whole handler,
 * does not affect routes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataModel {
    Class<?> value();
}
