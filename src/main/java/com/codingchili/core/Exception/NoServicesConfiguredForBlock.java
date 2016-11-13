package com.codingchili.core.Exception;

import com.codingchili.core.Configuration.Strings;

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
