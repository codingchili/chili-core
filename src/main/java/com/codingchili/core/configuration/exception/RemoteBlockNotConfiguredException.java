package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when the requested service block is not configured.
 */
public class RemoteBlockNotConfiguredException extends CoreException {
    public RemoteBlockNotConfiguredException(String remote, String block) {
        super(Strings.getRemoteBlockNotConfigured(remote, block));
    }
}
