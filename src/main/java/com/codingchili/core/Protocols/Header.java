package com.codingchili.core.Protocols;

/**
 * @author Robin Duda
 *         defines the type of a package.
 */
public class Header {
    private String action;

    public Header() {
    }

    public Header(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
