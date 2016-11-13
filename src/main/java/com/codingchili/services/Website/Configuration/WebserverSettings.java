package com.codingchili.services.Website.Configuration;

import com.codingchili.core.Configuration.ServiceConfigurable;

import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 */
public class WebserverSettings extends ServiceConfigurable {
    public static final String PATH_WEBSERVER = Strings.getService("webserver");
    private String startPage = "/index.html";
    private String missingPage = "/missing.html";
    private String resources = "website/";
    private boolean gzip = false;

    public boolean getGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public String getStartPage() {
        return startPage;
    }

    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }

    public String getMissingPage() {
        return missingPage;
    }

    public void setMissingPage(String missingPage) {
        this.missingPage = missingPage;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}
