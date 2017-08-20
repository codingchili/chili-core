package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when the requested service block is not configured.
 */
public class RemoteBlockNotConfiguredException extends CoreException {
    public RemoteBlockNotConfiguredException(String remote, String block) {
        super(CoreStrings.getRemoteBlockNotConfigured(remote, block));
    }
}
