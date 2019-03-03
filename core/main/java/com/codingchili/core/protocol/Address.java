package com.codingchili.core.protocol;

import java.lang.annotation.*;

/**
 * Alternate way of specifying a handlers listening address.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Address {
    String WEBSOCKET = "websocket";

    String value();
}
