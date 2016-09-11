package Authentication.Realm;

import Authentication.Controller.RealmRequest;
import Authentication.Controller.Transport.RealmConnection;
import Protocols.Authorization.Token;
import Realm.Configuration.RealmSettings;
import Shared.ResponseListener;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class RealmRequestMock implements RealmRequest {
    private ResponseListener listener;
    private JsonObject data;

    RealmRequestMock(JsonObject data, ResponseListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public RealmSettings realm() {
        return null;
    }

    @Override
    public int players() {
        return 0;
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
    public RealmConnection connection() {
        return null;
    }

    @Override
    public Token token() {
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
    public void error() {

    }

    @Override
    public void unauthorized() {

    }

    @Override
    public void write(Object object) {

    }

    @Override
    public void accept() {

    }

    @Override
    public void missing() {

    }

    @Override
    public void conflict() {

    }
}
