package com.codingchili.core.listener.transport;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.listener.ClusterHelper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 * <p>
 * A request in the cluster.
 * <p>
 * Size does not apply to these requests.
 */
public class ClusterRequest implements Request {
    private final Message message;
    private int timeout = Configurations.system().getClusterTimeout();
    private Buffer buffer;
    private JsonObject json;

    protected ClusterRequest(Request request) {
        this(((ClusterRequest) request).getMessage());
        this.timeout = request.timeout();
    }

    /**
     * Creates a cluster request from an eventbus message.
     *
     * @param message the eventbus message
     */
    public ClusterRequest(Message message) {
        this.message = message;

        if (message.body() instanceof Buffer) {
            this.buffer = (Buffer) message.body();
        }
        if (message.body() instanceof String) {
            this.json = new JsonObject((String) message.body());
        } else {
            this.json = (JsonObject) message.body();
        }
    }

    @Override
    public void write(Object object) {
        if (object != null) {
            ClusterHelper.reply(message, object);
        } else {
            accept();
        }
    }

    protected void send(ResponseStatus status, Throwable exception) {
        write(Protocol.response(status, exception));
    }

    protected void send(ResponseStatus status) {
        write(Protocol.response(status));
    }

    @Override
    public JsonObject data() {
        return json;
    }

    /**
     * @return the buffer attached to the requests when sending files.
     */
    public Buffer buffer() {
        return buffer;
    }

    @Override
    public int timeout() {
        return timeout;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int maxSize() {
        // override the default max value for clustered requests.
        return Integer.MAX_VALUE;
    }

    private Message getMessage() {
        return message;
    }
}
