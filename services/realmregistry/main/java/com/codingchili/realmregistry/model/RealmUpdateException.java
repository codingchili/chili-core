package com.codingchili.realmregistry.model;

import com.codingchili.common.Strings;

import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 */
public class RealmUpdateException extends CoreException {
    public RealmUpdateException() {
        super(Strings.ERROR_REALM_UPDATE);
    }
}
