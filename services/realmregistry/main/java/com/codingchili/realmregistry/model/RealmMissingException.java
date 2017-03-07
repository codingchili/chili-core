package com.codingchili.realmregistry.model;

import com.codingchili.common.Strings;

import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 */
public class RealmMissingException extends CoreException {
    public RealmMissingException() {
        super(Strings.ERROR_REALM_MISSING);
    }
}
