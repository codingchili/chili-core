package com.codingchili.services.Realm.Controller;

import io.vertx.core.json.JsonObject;

import com.codingchili.core.Protocol.*;

import com.codingchili.services.Authentication.Model.Account;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
class RealmRequest extends ClusterRequest {
    private final Request request;

    RealmRequest(Request request) {
        super(request);
        this.request = request;
    }

    public String account() {
        return token().getDomain();
    }

    public String character() {
        return data().getString(ID_CHARACTER);
    }

    public String className() {
        return data().getString(ID_CLASS);
    }

    public Account getAccount() {
        return Serializer.unpack(data().getJsonObject(ID_ACCOUNT), Account.class);
    }

    public void error(Throwable error) {
        request.write(new JsonObject()
                .put(PROTOCOL_STATUS, ResponseStatus.ERROR)
                .put(ID_MESSAGE, error.getMessage()));
    }

    public String instance() {
        return data().getString(ID_INSTANCE);
    }
}
