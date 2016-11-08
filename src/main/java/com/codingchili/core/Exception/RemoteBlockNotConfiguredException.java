package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class RemoteBlockNotConfiguredException extends CoreException{
    public RemoteBlockNotConfiguredException(String remote, String block) {
        super(Strings.getRemoteBlockNotConfigured(remote, block));
    }
}
