package com.codingchili.patching.configuration;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.ServiceConfigurable;

import static com.codingchili.common.Strings.DIR_RESOURCES;

/**
 * @author Robin Duda
 * Contains settings for the patchserver.
 */
public class PatchServerSettings extends ServiceConfigurable {
    public static final String PATH_PATCHSERVER = Strings.getService("patchserver");
    private GameInfo gameinfo = new GameInfo();
    private PatchNotes patchNotes = new PatchNotes();
    private NewsList news = new NewsList();
    private Boolean cache = false;
    private boolean gzip = false;
    private String directory = DIR_RESOURCES;

    public GameInfo getGameinfo() {
        return gameinfo;
    }

    public void setGameinfo(GameInfo gameinfo) {
        this.gameinfo = gameinfo;
    }

    public PatchNotes getPatchNotes() {
        return patchNotes;
    }

    public void setPatchNotes(PatchNotes patchNotes) {
        this.patchNotes = patchNotes;
    }

    public Boolean getCache() {
        return cache;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public NewsList getNews() {
        return news;
    }

    public void setNews(NewsList news) {
        this.news = news;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}