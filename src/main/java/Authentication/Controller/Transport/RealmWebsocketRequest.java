package Authentication.Controller.Transport;

import Authentication.Controller.RealmRequest;
import Configuration.RealmSettings;
import Protocol.RealmUpdate;
import Utilities.Serializer;
import Utilities.Token;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */

class RealmWebsocketRequest implements RealmRequest {
    private RealmConnection connection;
    private JsonObject data;

    RealmWebsocketRequest(RealmConnection connection, JsonObject data) {
        this.connection = connection;
        this.data = data;
    }

    public RealmWebsocketRequest(RealmConnection connection) {
        this(connection, null);
    }

    @Override
    public RealmSettings realm() {
        return (RealmSettings) Serializer.unpack(data.getJsonObject("realmName"), RealmSettings.class);
    }

    @Override
    public RealmUpdate update() {
        return (RealmUpdate) Serializer.unpack(data.getJsonObject("realmName"), RealmUpdate.class);
    }

    @Override
    public boolean authorized() {
        return connection.authenticated();
    }

    @Override
    public void write(Object object) {
        connection.write(object);
    }

    @Override
    public void error() {
        connection.write(new JsonObject().put("accepted", false).encode());
    }

    @Override
    public String realmName() {
        return connection.realm();
    }

    @Override
    public String sender() {
        return connection.socket().remoteAddress().host();
    }


    @Override
    public RealmConnection connection() {
        return connection;
    }

    @Override
    public void accept() {
        connection.write(new JsonObject().put("accepted", true).encode());
    }

    @Override
    public void missing() {

    }

    @Override
    public void conflict() {

    }

    @Override
    public Token token() {
        return (Token) Serializer.unpack(data.getJsonObject("token"), Token.class);
    }

    @Override
    public String account() {
        return data.getString("account");
    }

    @Override
    public String name() {
        return data.getString("characterName");
    }

    @Override
    public void unauthorized() {
        connection.write(new JsonObject().put("error", true));
    }
}
