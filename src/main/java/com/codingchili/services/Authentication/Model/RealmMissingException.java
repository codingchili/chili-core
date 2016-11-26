package com.codingchili.services.authentication.model;

import com.codingchili.services.Shared.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 */
public class RealmMissingException extends CoreException {
    public RealmMissingException() {
        super(Strings.ERROR_REALM_MISSING);
    }
}
