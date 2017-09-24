package com.codingchili.website.configuration;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.ServiceConfigurable;

/**
 * @author Robin Duda
 * <p>
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

    /**
     * @return true if gzip is enabled.
     */
    public boolean getGzip() {
        return gzip;
    }

    /**
     * @param gzip indicates whether gzip is to be used.
     * @return fluent
     */
    public WebserverSettings setGzip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    /**
     * @return the path to the start page.
     */
    public String getStartPage() {
        return startPage;
    }

    /**
     * @param startPage path to the start page
     * @return fluent
     */
    public WebserverSettings setStartPage(String startPage) {
        this.startPage = startPage;
        return this;
    }

    /**
     * @return path to the missing page
     */
    public String getMissingPage() {
        return missingPage;
    }

    /**
     * @param missingPage path to the missing page
     * @return fluent
     */
    public WebserverSettings setMissingPage(String missingPage) {
        this.missingPage = missingPage;
        return this;
    }

    /**
     * @return a path to the resources directory
     */
    public String getResources() {
        return resources;
    }

    /**
     * @param resources set the path to the resources directory.
     * @return fluent
     */
    public WebserverSettings setResources(String resources) {
        this.resources = resources;
        return this;
    }
}
