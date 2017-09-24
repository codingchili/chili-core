package com.codingchili.patching.configuration;

import java.time.Instant;

/**
 * @author Robin Duda
 * Contains configuration data for a news object on the website.
 */
class NewsItem {
    private String title = "title";
    private String content = "content";
    private String date = Instant.now().toString();

    public String getDate() {
        return date;
    }

    protected void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    protected void setContent(String content) {
        this.content = content;
    }
}
