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
    private boolean gzip;

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
}
