package com.codingchili.core.listener.transport;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.logging.RemoteLogger;

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

}
