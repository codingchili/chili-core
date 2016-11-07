package com.codingchili.services.Patching.Configuration;

/**
 * @author Robin Duda
 *         Contains configuration data for a news object on the website.
 */
class NewsItem {
    private String title;
    private String content;
    private String date;

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
