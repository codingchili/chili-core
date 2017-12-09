package com.codingchili.core.listener;

import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.listener.ListenerSettings.DEFAULT_MAX_REQUEST_BYTES;
import static com.codingchili.core.listener.ListenerSettings.DEFAULT_TIMEOUT;

/**
 * @author Robin Duda
 * <p>
 * Base request class.
 */
public interface Request extends Messageable {
    String TARGET_UNDEFINED = ID_UNDEFINED;

    /**
     * Get the route from the request, the route specifies with method that should
     * be invoked.
     *
     * @return the requested route
     */
    default String route() {
        String route = data().getString(PROTOCOL_ROUTE);

        if (route == null) {
            route = DIR_SEPARATOR;
        }
        return route;
    }

    /**
     * The target node of the request. The target specifies which service or which
     * sub-service that the requested route resides in.
     *
     * @return the target node
     */
    default String target() {
        String target = data().getString(PROTOCOL_TARGET);

        if (target == null) {
            target = TARGET_UNDEFINED;
        }
        return target;
    }

    /**
     * Get the request token sent with the request.
     *
     * @return the requests token
     */
    default Token token() {
        if (data().containsKey(ID_TOKEN)) {
            return Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class);
        } else {
            return new Token();
        }
    }

    /**
     * Get the raw data of the request as a json object
     *
     * @return the raw data of the request
     */
    JsonObject data();

    /**
     * Get the request timeout which indicates how long the sender is waiting until
     * the request is considered to have timed out.
     *
     * defaults to #{@link ListenerSettings#DEFAULT_TIMEOUT}
     *
     * @return milliseconds specifying the timeout of the request
     */
    default int timeout() {
        return DEFAULT_TIMEOUT;
    }

    /**
     * @return the size of the request in bytes.
     */
    int size();

    /**
     * defaults to #{@link ListenerSettings#DEFAULT_MAX_REQUEST_BYTES}
     *
     * @return the maximum number of bytes allowed in a single request.
     */
    default int maxSize() {
        return DEFAULT_MAX_REQUEST_BYTES;
    }
}
