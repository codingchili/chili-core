package com.codingchili.core.configuration.exception;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * @author Robin Duda
 * <p>
 * Throw when an executing block has zero configured services.
 */
public class NoServicesConfiguredForBlock extends CoreException {

    /**
     * @param block the name of the block that no services are configured for.
     */
    public NoServicesConfiguredForBlock(String block) {
        super(CoreStrings.getNoServicesConfiguredForBlock(block));
    }
}
