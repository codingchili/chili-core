package com.codingchili.website.configuration;

import com.codingchili.common.Strings;

import com.codingchili.core.configuration.ServiceConfigurable;

/**
 * @author Robin Duda
 *
 * Settings for the web server.
 */
public class WebserverSettings extends ServiceConfigurable {
    public static final String PATH_WEBSERVER = Strings.getService("webserver");
    private String startPage = "index.html";
    private String missingPage = "missing.html";
    private String resources = "website";
    private boolean gzip = false;

    public WebserverSettings() {
        super(PATH_WEBSERVER);
    }

    public boolean getGzip() {
        return gzip;
    }

    public WebserverSettings setGzip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    public String getStartPage() {
        return startPage;
    }

    public WebserverSettings setStartPage(String startPage) {
        this.startPage = startPage;
        return this;
    }

    public String getMissingPage() {
        return missingPage;
    }

    public WebserverSettings setMissingPage(String missingPage) {
        this.missingPage = missingPage;
        return this;
    }

    public String getResources() {
        return resources;
    }

    public WebserverSettings setResources(String resources) {
        this.resources = resources;
        return this;
    }
}
