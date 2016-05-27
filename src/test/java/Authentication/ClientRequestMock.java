package Authentication;

import Authentication.Controller.ClientRequest;
import Authentication.Model.Account;
import Utilities.Serializer;
import Utilities.Token;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class ClientRequestMock implements ClientRequest {
    private ResponseListener listener;
    private JsonObject data;

    public ClientRequestMock(JsonObject data, ResponseListener listener) {
        this.data = data;
        this.listener = listener;
    }


    @Override
    public String realm() {
        return data.getString("realm");
    }

    @Override
    public String account() {
        return data.getString("name");
    }

    @Override
    public String character() {
        return data.getString("character");
    }

    @Override
    public String className() {
        return data.getString("class");
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
    public void unauthorize() {
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

    public enum ResponseStatus {UNAUTHORIZED, MISSING, CONFLICT, ACCEPTED, ERROR};
}
