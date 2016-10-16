package com.codingchili.core.Configuration.System;

import com.codingchili.core.Configuration.LoadableConfigurable;

import static com.codingchili.core.Configuration.Strings.PATH_DEPLOY;

/**
 * @author Robin Duda
 */
public class DeploySettings implements LoadableConfigurable {
    @Override
    public String getPath() {
        return PATH_DEPLOY;
    }
}
