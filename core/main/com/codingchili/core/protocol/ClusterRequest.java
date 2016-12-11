package com.codingchili.core.protocol;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.security.Token;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *
 * A request in the cluster.
 */
public class ClusterRequest extends BaseRequest {
    private int timeout = 3000;
    private Buffer buffer;
    private final JsonObject json;
    private final Message message;

    protected ClusterRequest(Request request) {
        this(((ClusterRequest) request).getMessage());
        this.timeout = request.timeout();
    }

    /**
     * Creates a cluster request from an eventbus message.
     * @param message the eventbus message
     */
    public ClusterRequest(Message message) {
        if (message.body() instanceof Buffer) {
            this.buffer = (Buffer) message.body();
        }
        if (message.body() instanceof String) {
            this.json = new JsonObject((String) message.body());
        } else {
            this.json = (JsonObject) message.body();
        }

        this.message = message;
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

    private Message getMessage() {
        return message;
    }
}
