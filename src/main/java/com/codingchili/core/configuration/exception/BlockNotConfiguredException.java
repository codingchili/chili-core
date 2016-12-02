package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when a requested service block is not configured.
 */
public class BlockNotConfiguredException extends CoreException {
    public BlockNotConfiguredException(String block) {
        super(CoreStrings.getBlockNotConfigured(block));
    }
}
