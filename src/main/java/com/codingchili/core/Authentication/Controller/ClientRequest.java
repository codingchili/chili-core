package com.codingchili.core.Authentication.Controller;

import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.Util.Token;

/**
 * @author Robin Duda
 */


public interface ClientRequest extends Request {
    String realmName();

    String account();

    String character();

    String className();

    String sender();

    Token token();

    Account getAccount();
}
