package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Throw when the requested service block is not configured.
 */
public class RemoteBlockNotConfiguredException extends CoreException{
    public RemoteBlockNotConfiguredException(String remote, String block) {
        super(Strings.getRemoteBlockNotConfigured(remote, block));
    }
}
