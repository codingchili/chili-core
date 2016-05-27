package Authentication.Controller;

import Configuration.RealmSettings;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class RealmWebsocketRequest implements RealmRequest {
    private RealmConnection connection;
    private JsonObject data;

    public RealmWebsocketRequest(RealmConnection connection, JsonObject data) {
        this.connection = connection;
        this.data = data;
    }


    @Override
    public RealmSettings realm() {
        return null;
    }

    @Override
    public boolean authorized() {
        return false;
    }

    @Override
    public void write(Object object) {

    }

    @Override
    public void error() {

    }

    @Override
    public String realmName() {
        return null;
    }

    @Override
    public String sender() {
        return null;
    }

    @Override
    public String account() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public RealmConnection connection() {
        return null;
    }
}
