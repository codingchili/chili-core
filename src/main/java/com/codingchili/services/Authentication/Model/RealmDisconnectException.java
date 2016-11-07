package com.codingchili.services.Authentication.Model;

import com.codingchili.services.Shared.Strings;
import com.codingchili.core.Exception.CoreException;

/**
 * @author Robin Duda
 */
public class RealmDisconnectException extends CoreException {
    public RealmDisconnectException() {
        super(Strings.ERROR_REALM_DISCONNECT);
    }
}
