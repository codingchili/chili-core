package com.codingchili.realm.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.transport.ClusterRequest;
import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Account;

import static com.codingchili.common.Strings.*;

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

    public String instance() {
        return data().getString(ID_INSTANCE);
    }
}
