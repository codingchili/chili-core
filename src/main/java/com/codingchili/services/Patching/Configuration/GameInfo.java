package com.codingchili.services.patching.configuration;

/**
 * @author Robin Duda
 * configuration file holds content and title of the website.
 */
class GameInfo {
    private String content = "content";
    private String title = "title";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    protected void setContent(String content) {
        this.content = content;
    }
}