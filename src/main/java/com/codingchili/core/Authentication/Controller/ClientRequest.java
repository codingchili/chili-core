package com.codingchili.core.Authentication.Controller;

import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.ClusterRequest;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Protocols.Util.Token;

import static com.codingchili.core.Configuration.Strings.ID_ACCOUNT;
import static com.codingchili.core.Configuration.Strings.ID_TOKEN;


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
