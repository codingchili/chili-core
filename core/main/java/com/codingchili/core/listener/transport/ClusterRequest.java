package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Response;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * A request in the cluster.
 * <p>
 * Size does not apply to these requests.
 */
public class ClusterRequest implements Request {
    private Connection connection;
    private final Message message;
    private int timeout = Configurations.system().getClusterTimeout();
    private Buffer buffer;
    private JsonObject json;

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
        }

        if (message.body() instanceof JsonObject) {
            this.json = (JsonObject) message.body();
        }
        // else: passing non supported types - requires special handling in listener.
    }

    @Override
    public void write(Object msg) {
        if (msg != null) {
            message.reply(Response.json(target(), route(), msg));
        } else {
            accept();
        }
    }

    @Override
    public Connection connection() {
        if (connection == null) {
            connection = new Connection((message) -> {
                throw new UnsupportedOperationException("Cannot write to the connection of Cluster requests, use #write instead.");
            }, "");
            connection.setProperty(PROTOCOL_CONNECTION, json.getString(PROTOCOL_CONNECTION));
            connection.setProperty(Connection.ID, json.getString(Connection.ID));
        }
        return connection;
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
}
