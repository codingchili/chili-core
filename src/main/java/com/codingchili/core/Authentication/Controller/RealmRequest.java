package com.codingchili.core.Authentication.Controller;

import com.codingchili.core.Routing.Controller.Transport.RealmConnection;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Token;

/**
 * @author Robin Duda
 */
public interface RealmRequest extends Request {

    RealmSettings realm();

    int players();

    String realmName();

    String sender();

    RealmConnection connection();

    Token token();

    String account();

    String name();
}
