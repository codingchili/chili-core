package com.codingchili.realmregistry.model;

import com.codingchili.common.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 */
public class RealmDisconnectException extends CoreException {
    public RealmDisconnectException() {
        super(Strings.ERROR_REALM_DISCONNECT);
    }
}
