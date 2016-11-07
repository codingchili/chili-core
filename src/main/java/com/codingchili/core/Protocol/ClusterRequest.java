package com.codingchili.core.Protocol;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Security.Token;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
public class ClusterRequest implements Request {
    private int timeout = 3000;
    private Buffer buffer;
    private final JsonObject json;
    private final Message message;

    protected ClusterRequest(Request request) {
        this(((ClusterRequest) request).getMessage());
        this.timeout = request.timeout();
    }

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
    public void error(Throwable e) {
        message.reply(message(ResponseStatus.ERROR).put(ID_MESSAGE, e.getMessage()));
    }

    private JsonObject message(ResponseStatus message) {
        return new JsonObject().put(PROTOCOL_STATUS, message);
    }

    private JsonObject message(ResponseStatus message, Throwable exception) {
        return new JsonObject()
                .put(PROTOCOL_STATUS, message)
                .put(ID_MESSAGE, exception.getMessage());
    }

    @Override
    public void unauthorized(Throwable e) {
        message.reply(message(ResponseStatus.UNAUTHORIZED, e));
    }

    @Override
    public void accept() {
        message.reply(message(ResponseStatus.ACCEPTED));
    }

    @Override
    public void missing(Throwable e) {
        message.reply(message(ResponseStatus.MISSING, e));
    }

    @Override
    public void conflict(Throwable e) {
        message.reply(message(ResponseStatus.CONFLICT, e));
    }

    @Override
    public void bad(Throwable e) {
        message.reply(message(ResponseStatus.BAD, e));
    }

    @Override
    public String action() {
        return json.getString(ID_ACTION);
    }

    @Override
    public String target() {
        return json.getString(ID_TARGET);
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
    public Token token() {
        if (json.containsKey(ID_TOKEN)) {
            return Serializer.unpack(json.getJsonObject(ID_TOKEN), Token.class);
        } else {
            return new Token();
        }
    }

    @Override
    public JsonObject data() {
        return json;
    }

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
