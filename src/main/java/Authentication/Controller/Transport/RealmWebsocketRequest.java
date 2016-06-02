package Authentication.Controller.Transport;

import Authentication.Controller.RealmRequest;
import Configuration.Gameserver.RealmSettings;
import Protocols.Authentication.RealmUpdate;
import Protocols.Serializer;
import Protocols.Authorization.Token;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */

class RealmWebsocketRequest implements RealmRequest {
    private RealmConnection connection;
    private JsonObject data;
    private JsonObject realm;

    RealmWebsocketRequest(RealmConnection connection, JsonObject data) {
        this.connection = connection;
        this.data = data;
        this.realm = data.getJsonObject("realm");
    }

    RealmWebsocketRequest(RealmConnection connection) {
        this(connection, null);
    }

    @Override
    public RealmSettings realm() {
        return (RealmSettings) Serializer.unpack(realm, RealmSettings.class);
    }

    @Override
    public int players() {
        return data.getInteger("players");
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
        return data.getString("connection");
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
        connection.write(new JsonObject().put("error", true));
    }

    @Override
    public void conflict() {

    }

    @Override
    public Token token() {
        JsonObject authentication = realm.getJsonObject("authentication");

        if (authentication.containsKey("token")) {
            return (Token) Serializer.unpack(authentication.getJsonObject("token"), Token.class);
        } else {
            return null;
        }
    }

    @Override
    public String account() {
        return data.getString("account");
    }

    @Override
    public String name() {
        return data.getString("name");
    }

    @Override
    public void unauthorized() {
        connection.write(new JsonObject().put("error", true));
    }
}
