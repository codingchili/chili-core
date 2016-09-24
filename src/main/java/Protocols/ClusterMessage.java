package Protocols;

import Protocols.Authorization.Token;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 */
class ClusterMessage implements Request {
    private JsonObject body;
    private Message message;

    ClusterMessage(Message message) {
        this.body = (JsonObject) message.body();
        this.message =message;
    }

    @Override
    public void error() {
        message.reply(message(PROTOCOL_ACTION, PROTOCOL_ERROR));
    }

    private JsonObject message(String action, String message) {
        return new JsonObject().put(action, message);
    }

    @Override
    public void unauthorized() {
        message.reply(message(PROTOCOL_ACTION, PROTOCOL_UNAUTHORIZED));
    }

    @Override
    public void write(Object object) {
        message.reply(object);
    }

    @Override
    public void accept() {
        message.reply(message(PROTOCOL_ACTION, PROTOCOL_ACCEPTED));
    }

    @Override
    public void missing() {
        message.reply(message(PROTOCOL_ACTION, PROTOCOL_MISSING));
    }

    @Override
    public void conflict() {
        message.reply(message(PROTOCOL_ACTION, PROTOCOL_CONFLICT));
    }

    @Override
    public String action() {
        return body.getString(ID_ACTION);
    }

    @Override
    public Token token() {
        return Serializer.unpack(body.getString(ID_TOKEN), Token.class);
    }
}
