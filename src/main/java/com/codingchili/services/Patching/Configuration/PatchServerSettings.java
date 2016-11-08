package com.codingchili.services.Patching.Configuration;

import com.codingchili.core.Configuration.ServiceConfigurable;
import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 *         Contains settings for the patchserver.
 */
public class PatchServerSettings extends ServiceConfigurable {
    public static final String PATH_PATCHSERVER = Strings.getService("patchserver");
    private GameInfo gameinfo;
    private PatchNotes patchNotes;
    private NewsList news;
    private Boolean cache;
    private boolean gzip;

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
}