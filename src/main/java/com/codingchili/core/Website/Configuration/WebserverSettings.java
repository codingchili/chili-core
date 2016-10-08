package com.codingchili.core.Website.Configuration;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 */
public class WebserverSettings implements Configurable {
    private RemoteAuthentication logserver;
    private RemoteAuthentication patchserver;
    private boolean cache;
    private boolean compress;

    public void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    public boolean getCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public boolean getCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public RemoteAuthentication getPatchserver() {
        return patchserver;
    }

    public void setPatchserver(RemoteAuthentication patchserver) {
        this.patchserver = patchserver;
    }

    @Override
    public String getPath() {
        return Strings.PATH_WEBSERVER;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }

    @Override
    public RemoteAuthentication getLogserver() {
        return logserver;
    }
}
