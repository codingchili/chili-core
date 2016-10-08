package com.codingchili.core.Authentication.Controller;

import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Token;
import com.codingchili.core.Realm.Configuration.RealmSettings;

/**
 * @author Robin Duda
 */
public interface AuthenticationRequest extends Request {

    RealmSettings realm();

    int players();

    String realmName();

    String sender();

    Token token();

    String account();

    String name();
}
