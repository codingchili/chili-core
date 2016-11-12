package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Throw when a requested service block is not configured.
 */
public class BlockNotConfiguredException extends CoreException {
    public BlockNotConfiguredException(String block) {
        super(Strings.getBlockNotConfigured(block));
    }
}
