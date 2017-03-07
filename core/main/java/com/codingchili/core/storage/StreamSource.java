package com.codingchili.core.storage;

import java.util.stream.Stream;

/**
 * @author Robin Duda
 *
 * Simple class to get a new stream to a source.
 * Allows the receiver of a StreamSource to renew
 * the streams it has been given. Wildly useful.
 */
interface StreamSource <T> {
    Stream<T> stream();
}
