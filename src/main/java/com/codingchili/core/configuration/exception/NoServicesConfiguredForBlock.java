package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.Strings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 *
 * Throw when an executing block has zero configured services.
 */
public class NoServicesConfiguredForBlock extends CoreException {
    public NoServicesConfiguredForBlock(String block) {
        super(Strings.getNoServicesConfiguredForBlock(block));
    }
}
