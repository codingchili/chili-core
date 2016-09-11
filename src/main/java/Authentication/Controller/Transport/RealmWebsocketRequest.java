package Authentication.Controller.Transport;

import Authentication.Controller.RealmRequest;
import Configuration.Strings;
import Realm.Configuration.RealmSettings;
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
        this.realm = data.getJsonObject(Strings.ID_REALM);
    }

    RealmWebsocketRequest(RealmConnection connection) {
        this(connection, null);
    }

    @Override
    public RealmSettings realm() {
        return Serializer.unpack(realm, RealmSettings.class);
    }

    @Override
    public int players() {
        return data.getInteger(Strings.ID_PLAYERS);
    }

    @Override
    public void write(Object object) {
        connection.write(object);
    }

    @Override
    public void error() {
        connection.write(new JsonObject().put(Strings.PROTOCOL_ACCEPTED, false).encode());
    }

    @Override
    public String realmName() {
        return connection.realm();
    }

    @Override
    public String sender() {
        return data.getString(Strings.PROTOCOL_CONNECTION);
    }


    @Override
    public RealmConnection connection() {
        return connection;
    }

    @Override
    public void accept() {
        connection.write(new JsonObject().put(Strings.PROTOCOL_ACCEPTED, true).encode());
    }

    @Override
    public void missing() {
        connection.write(new JsonObject().put(Strings.PROTOCOL_ERROR, true));
    }

    @Override
    public void conflict() {

    }

    @Override
    public Token token() {
        JsonObject authentication = realm.getJsonObject(Strings.PROTOCOL_AUTHENTICATION);

        if (authentication.containsKey(Strings.ID_TOKEN)) {
            return Serializer.unpack(authentication.getJsonObject(Strings.ID_TOKEN), Token.class);
        } else {
            return null;
        }
    }

    @Override
    public String account() {
        return data.getString(Strings.ID_ACCOUNT);
    }

    @Override
    public String name() {
        return data.getString(Strings.ID_NAME);
    }

    @Override
    public void unauthorized() {
        connection.write(new JsonObject().put(Strings.PROTOCOL_ERROR, true));
    }
}
