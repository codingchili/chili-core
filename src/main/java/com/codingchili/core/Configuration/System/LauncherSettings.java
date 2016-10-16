package com.codingchili.core.Configuration.System;

import com.codingchili.core.Configuration.LoadableConfigurable;

import static com.codingchili.core.Configuration.Strings.PATH_LAUNCHER;

/**
 * @author Robin Duda
 */
public class LauncherSettings implements LoadableConfigurable {
    @Override
    public String getPath() {
        return PATH_LAUNCHER;
    }
}
