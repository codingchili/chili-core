package com.codingchili.authentication.controller;


import com.codingchili.common.Strings;

import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Account;

import static com.codingchili.common.Strings.ID_ACCOUNT;

/**
 * @author Robin Duda
 *
 * Client authentication request.
 */
class ClientRequest extends ClusterRequest {

    ClientRequest(Request request) {
        super(request);
    }

    public String sender() {
        return data().getString(Strings.PROTOCOL_CONNECTION);
    }

    public Account getAccount() {
        return Serializer.unpack(data().getJsonObject(ID_ACCOUNT), Account.class);
    }
}
