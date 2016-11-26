package com.codingchili.services.authentication.controller;


import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Token;

import com.codingchili.services.authentication.model.Account;
import com.codingchili.services.Shared.Strings;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */


class ClientRequest extends ClusterRequest {

    ClientRequest(Request request) {
        super(request);
    }

    public String realmName() {
        return data().getString(Strings.ID_REALM);
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

    public String sender() {
        return data().getString(Strings.PROTOCOL_CONNECTION);
    }

    public Token token() {
        return Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class);
    }

    public Account getAccount() {
        return Serializer.unpack(data().getJsonObject(ID_ACCOUNT), Account.class);
    }
}
