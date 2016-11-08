package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class BlockNotConfiguredException extends CoreException {
    public BlockNotConfiguredException(String block) {
        super(Strings.getBlockNotConfigured(block));
    }
}
