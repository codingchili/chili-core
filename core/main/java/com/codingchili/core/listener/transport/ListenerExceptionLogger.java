package com.codingchili.core.listener.transport;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.logging.Level;
import io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClosedException;

import java.nio.channels.ClosedChannelException;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.logging.RemoteLogger;

import static com.codingchili.core.configuration.CoreStrings.LOG_CONNECTION_FAILED;
import static com.codingchili.core.configuration.CoreStrings.LOG_LISTENER;

/**
 * Logger with additional metadata used for listeners to provide additional
 * context information such as the attached handler.
 */
public class ListenerExceptionLogger {

    /**
     * @param core     the core context.
     * @param listener the listener that the logger is created for.
     * @param handler  the handler that is attached to the listener.
     * @return a logger with metadata which includes information about the handler/listener.
     */
    public static Logger create(CoreContext core, CoreListener listener, CoreHandler handler) {
        Logger logger = new RemoteLogger(core, handler.getClass());
        logger.setMetadataValue(LOG_LISTENER, listener.getClass()::getSimpleName);
        return logger;
    }

    /**
     * @param logger the logger of a listener.
     * @return an exception handler for its servers and connections: what the peer causes (it closed the connection,
     * sent a corrupt or oversized websocket frame) is logged at DEBUG, anything else as an error. A client must not
     * be able to fill the error log by connecting and hanging up.
     */
    public static Handler<Throwable> handler(Logger logger) {
        return e -> {
            if (peer(e)) {
                logger.event(LOG_CONNECTION_FAILED, Level.DEBUG).send(e.getClass().getSimpleName() + ": " + e.getMessage());
            } else {
                logger.onError(e);
            }
        };
    }

    /**
     * @param e an exception of a connection.
     * @return true if the peer caused it: a closed connection or a websocket frame it should not have sent.
     */
    static boolean peer(Throwable e) {
        return e instanceof ClosedChannelException || e instanceof HttpClosedException
                || e instanceof CorruptedWebSocketFrameException
                || (e instanceof java.io.IOException && String.valueOf(e.getMessage()).contains("Connection reset"));
    }
}
