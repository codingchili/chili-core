package com.codingchili.core.listener.transport;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.BaseRequest;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;

/**
 * @author Robin Duda
 *         <p>
 *         A request in the cluster.
 *
 *         Size does not apply to these requests.
 */
public class ClusterRequest extends BaseRequest {
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
    }

    @Override
    public void init() {
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
            if (object instanceof Buffer) {
                message.reply(object);
            } else {
                JsonObject reply;

                if (object instanceof JsonObject) {
                    reply = (JsonObject) object;
                } else {
                    reply = Serializer.json(object);
                }
                if (!reply.containsKey(PROTOCOL_STATUS)) {
                    reply.put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED);
                }
                message.reply(reply);
            }
        } else {
            accept();
        }
    }

    @Override
    protected void send(ResponseStatus status, Throwable exception) {
        write(Protocol.response(status, exception));
    }

    @Override
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
        return Integer.MAX_VALUE;
    }

    private Message getMessage() {
        return message;
    }
}
