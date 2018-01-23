package com.codingchili.realm.controller;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Account;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 */
class RealmRequest extends RequestWrapper {

    RealmRequest(Request request) {
        super(request);
    }

    public String account() {
        return token().getDomain();
    }

    public String character() {
        return data().getString(ID_CHARACTER);
    }

    public String className() {
        return data().getString(ID_PLAYERCLASS);
    }

    public Account getAccount() {
        return Serializer.unpack(data().getJsonObject(ID_ACCOUNT), Account.class);
    }

    public String instance() {
        return data().getString(ID_INSTANCE);
    }
}
