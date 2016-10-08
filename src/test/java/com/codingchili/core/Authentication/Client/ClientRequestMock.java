package com.codingchili.core.Authentication.Client;

import com.codingchili.core.Authentication.Controller.ClientRequest;
import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.Protocols.Util.Serializer.unpack;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
class ClientRequestMock implements ClientRequest {
    private ResponseListener listener;
    private JsonObject data = new JsonObject();
    private String action;

    ClientRequestMock(String action, ResponseListener listener, JsonObject data) {
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
    public String target() {
        return null;
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
}
