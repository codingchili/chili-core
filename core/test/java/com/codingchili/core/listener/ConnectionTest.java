package com.codingchili.core.listener;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.listener.transport.Connection;

/**
 * Tests closing a connection from the server side.
 */
public class ConnectionTest {

    @Test
    public void closeRunsTheCloserOnce() {
        AtomicInteger closed = new AtomicInteger();
        AtomicInteger handlers = new AtomicInteger();
        Connection connection = new Connection(message -> {}, "id")
                .closer(closed::incrementAndGet)
                .onCloseHandler(handlers::incrementAndGet);

        connection.close();
        connection.close();

        Assert.assertTrue(connection.isClosed());
        Assert.assertEquals(1, closed.get());
        // the transport runs the close handlers when it reports the close.
        Assert.assertEquals(0, handlers.get());
    }

    @Test
    public void closeWithoutTransportRunsTheHandlersOnce() {
        AtomicInteger handlers = new AtomicInteger();
        Connection connection = new Connection(message -> {}, "id")
                .onCloseHandler(handlers::incrementAndGet);

        Assert.assertFalse(connection.isClosed());
        connection.close();
        connection.close();

        Assert.assertTrue(connection.isClosed());
        Assert.assertEquals(1, handlers.get());
    }
}
