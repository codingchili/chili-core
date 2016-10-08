package com.codingchili.core.Realm.Controller;

import com.codingchili.core.Protocols.Realm.CharacterRequest;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Shared.ResponseListener;
import com.codingchili.core.Shared.ResponseStatus;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.Configuration.Strings.ID_TOKEN;

/**
 * @author Robin Duda
 */
public class RealmRequestMock implements RealmRequest {
    private JsonObject data;
    private String action;
    private ResponseListener listener;

    public RealmRequestMock(String action, ResponseListener listener, JsonObject data) {
        this.action = action;
        this.data = (data == null) ? new JsonObject() : data;
        this.listener = listener;
    }

    @Override
    public void error() {
        listener.handle(null, ResponseStatus.ERROR);
    }

    @Override
    public void unauthorized() {
        listener.handle(null, ResponseStatus.UNAUTHORIZED);
    }

    @Override
    public void write(Object object) {
        listener.handle(Serializer.json(object), ResponseStatus.ACCEPTED);
    }

    @Override
    public void accept() {
        listener.handle(null, ResponseStatus.ACCEPTED);
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
        return 5000;
    }

    @Override
    public CharacterRequest characterRequest() {
        return null;
    }
}
