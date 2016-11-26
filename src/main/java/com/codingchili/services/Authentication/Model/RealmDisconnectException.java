package com.codingchili.services.authentication.model;

import com.codingchili.services.Shared.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 */
public class RealmDisconnectException extends CoreException {
    public RealmDisconnectException() {
        super(Strings.ERROR_REALM_DISCONNECT);
    }
}
