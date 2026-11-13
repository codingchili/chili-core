package com.codingchili.core.listener.transport;

import io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.vertx.core.http.HttpClosedException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.TimeUnit;

/**
 * What the peer of a connection causes is not an error of the listener.
 */
public class ListenerExceptionLoggerTest {
    @Rule
    public Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

    @Test
    public void thePeersDoingIsNotAnError() {
        Assert.assertTrue(ListenerExceptionLogger.peer(new ClosedChannelException()));
        Assert.assertTrue(ListenerExceptionLogger.peer(new HttpClosedException("closed")));
        Assert.assertTrue(ListenerExceptionLogger.peer(new CorruptedWebSocketFrameException(
                WebSocketCloseStatus.MESSAGE_TOO_BIG, "Max frame length of 65536 has been exceeded.")));
        Assert.assertTrue(ListenerExceptionLogger.peer(new java.io.IOException("Connection reset by peer")));
    }

    @Test
    public void faultsAre() {
        Assert.assertFalse(ListenerExceptionLogger.peer(new NullPointerException()));
        Assert.assertFalse(ListenerExceptionLogger.peer(new IllegalStateException("broken")));
        Assert.assertFalse(ListenerExceptionLogger.peer(new java.io.IOException("No space left on device")));
    }
}
