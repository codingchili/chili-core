package com.codingchili.services.Authentication.Model;

import com.codingchili.services.Shared.Strings;
import com.codingchili.core.Exception.CoreException;

/**
 * @author Robin Duda
 */
public class RealmUpdateException extends CoreException {
    public RealmUpdateException() {
        super(Strings.ERROR_REALM_UPDATE);
    }
}
