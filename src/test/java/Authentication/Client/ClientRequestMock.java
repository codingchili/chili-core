package Authentication.Client;

import Authentication.Controller.ClientRequest;
import Authentication.Model.Account;
import Shared.ResponseListener;
import Protocols.Authentication.ClientAuthentication;
import Protocols.Serializer;
import Protocols.Authorization.Token;
import Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
class ClientRequestMock implements ClientRequest {
    private ResponseListener listener;
    private JsonObject data;

    ClientRequestMock(JsonObject data, ResponseListener listener) {
        this.data = data;
        this.listener = listener;
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
        return (Token) Serializer.unpack(data.getJsonObject("token"), Token.class);
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
    public void accept() {
        listener.handle(null, ResponseStatus.ACCEPTED);
    }

    @Override
    public void error() {
        listener.handle(null, ResponseStatus.ERROR);
    }

    @Override
    public Account getAccount() {
        return (Account) Serializer.unpack(data.getJsonObject("account"), Account.class);
    }

    @Override
    public void authenticate(ClientAuthentication authentication) {
        listener.handle(Serializer.json(authentication), ResponseStatus.ACCEPTED);
    }
}
