package com.codingchili.core.Protocols;

import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
public class ClusterRequest implements Request {
    private Buffer buffer;
    private JsonObject json;
    private Message message;

    public ClusterRequest(Request request) {
        this(((ClusterRequest) request).getMessage());
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
    public void error() {
        message.reply(message(ResponseStatus.ERROR));
    }

    private JsonObject message(ResponseStatus message) {
        return new JsonObject().put(PROTOCOL_STATUS, message);
    }

    @Override
    public void unauthorized() {
        message.reply(message(ResponseStatus.UNAUTHORIZED));
    }

    @Override
    public void write(Object object) {
        if (object instanceof Buffer) {
            message.reply(object);
        } else {
            message.reply(Serializer.json(object).put(PROTOCOL_STATUS, ResponseStatus.ACCEPTED));
        }
    }

    @Override
    public void accept() {
        message.reply(message(ResponseStatus.ACCEPTED));
    }

    @Override
    public void missing() {
        message.reply(message(ResponseStatus.MISSING));
    }

    @Override
    public void conflict() {
        message.reply(message(ResponseStatus.CONFLICT));
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
        return 0;
    }

    public Message getMessage() {
        return message;
    }
}
