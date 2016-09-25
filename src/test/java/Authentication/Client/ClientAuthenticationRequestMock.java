package Authentication.Client;

import Authentication.Controller.ClientAuthenticationRequest;
import Authentication.Model.Account;
import Shared.ResponseListener;
import Protocols.Authentication.ClientAuthentication;
import Protocols.Serializer;
import Protocols.Authorization.Token;
import Shared.ResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import static Protocols.Serializer.unpack;

import static Configuration.Strings.*;

/**
 * @author Robin Duda
 */
class ClientAuthenticationRequestMock implements ClientAuthenticationRequest {
    private ResponseListener listener;
    private JsonObject data = new JsonObject();
    private String action;

    ClientAuthenticationRequestMock(JsonObject data, ResponseListener listener, String action) {
        this.data = data;
        this.listener = listener;
        this.action = action;
    }


    @Override
    public String realmName() {
        return data.getString("realmName");
    }

    @Override
    public String account() {
        return data.getJsonObject("token").getString("domain");
    }

    @Override
    public String character() {
        return data.getString("character");
    }

    @Override
    public String className() {
        return data.getString("className");
    }

    @Override
    public String sender() {
        return "null";
    }

    @Override
    public Token token() {
        if (data.containsKey(ID_TOKEN)) {
            return Serializer.unpack(data.getJsonObject(ID_TOKEN), Token.class);
        } else {
            return null;
        }
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return 0;
    }

    @Override
    public void write(Object object) {
        listener.handle(Serializer.json(object), ResponseStatus.ACCEPTED);
    }

    @Override
    public void unauthorized() {
        listener.handle(null, ResponseStatus.UNAUTHORIZED);
    }

    @Override
    public void missing() {
        listener.handle(null, ResponseStatus.MISSING);
    }

    @Override
    public void conflict() {
        listener.handle(null, ResponseStatus.CONFLICT);
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public void accept() {
        listener.handle(null, ResponseStatus.ACCEPTED);
    }

    @Override
    public void error() {
        listener.handle(null, ResponseStatus.ERROR);
    }

    @Override
    public Account getAccount() {
        return unpack(data.getJsonObject("account"), Account.class);
    }

    @Override
    public void authenticate(ClientAuthentication authentication) {
        listener.handle(Serializer.json(authentication), ResponseStatus.ACCEPTED);
    }
}
