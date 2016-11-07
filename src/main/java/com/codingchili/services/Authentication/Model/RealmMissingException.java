package com.codingchili.services.Authentication.Model;

import com.codingchili.services.Shared.Strings;
import com.codingchili.core.Exception.CoreException;

/**
 * @author Robin Duda
 */
public class RealmMissingException extends CoreException {
    public RealmMissingException() {
        super(Strings.ERROR_REALM_MISSING);
    }
}
