package com.codingchili.core.protocol;

import java.lang.annotation.*;
import java.util.function.Function;

import com.codingchili.core.listener.Receiver;

/**
 * An annotation that indicates that the annotated method should be picked up by
 * protocols when calling {@link Protocol#annotated(Receiver)}. This is an alternative
 * to calling {@link Protocol#authenticator(Function)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Authenticator {
}
