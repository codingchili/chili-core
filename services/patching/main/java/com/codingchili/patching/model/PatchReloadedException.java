package com.codingchili.patching.model;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 * @author Robin Duda
 * <p>
 * Throw when a patch version has changed during a patch session.
 */
public class PatchReloadedException extends CoreException {
    protected PatchReloadedException() {
        super(CoreStrings.ERROR_PATCH_RELOADED, ResponseStatus.CONFLICT);
    }
}
