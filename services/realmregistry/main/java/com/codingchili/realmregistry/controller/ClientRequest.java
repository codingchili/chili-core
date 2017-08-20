package com.codingchili.realmregistry.controller;

import com.codingchili.common.Strings;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.ID_TOKEN;

/**
 * @author Robin Duda
 */


class ClientRequest extends RequestWrapper {

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
}
