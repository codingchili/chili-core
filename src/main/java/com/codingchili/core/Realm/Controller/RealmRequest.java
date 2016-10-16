package com.codingchili.core.Realm.Controller;

import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.ClusterRequest;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.ResponseStatus;
import com.codingchili.core.Protocols.Util.Serializer;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 */
class RealmRequest extends ClusterRequest {
    private Request request;

    RealmRequest(Request request) {
        super(request);
        this.request = request;
    }

    public String account() {
        return token().getDomain();
    }

    public String character() {
        return data().getString(Strings.ID_CHARACTER);
    }

    public String className() {
        return data().getString(Strings.ID_CLASS);
    }

    public Account getAccount() {
        return Serializer.unpack(data().getJsonObject(ID_ACCOUNT), Account.class);
    }

    public void error(Throwable error) {
        request.write(new JsonObject()
                .put(PROTOCOL_STATUS, ResponseStatus.ERROR)
                .put(ID_MESSAGE, error.getMessage()));
    }
}
