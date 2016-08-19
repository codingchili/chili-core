package Patching.Configuration;

import Configuration.Configurable;
import Configuration.RemoteAuthentication;
import Configuration.Strings;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Robin Duda
 *         Contains settings for the webserver.
 */
@JsonIgnoreProperties({"name"})
public class PatchServerSettings implements Configurable {
    private RemoteAuthentication authserver;
    private RemoteAuthentication logserver;
    private GameInfo gameinfo;
    private PatchNotes patch;
    private NewsList news;
    private Integer port;
    private Boolean cache;
    private Boolean compress;

    @Override
    public String getPath() {
        return Strings.PATH_PATCHSERVER;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }

    public RemoteAuthentication getAuthserver() {
        return authserver;
    }

    public void setAuthserver(RemoteAuthentication authserver) {
        this.authserver = authserver;
    }

    public GameInfo getGameinfo() {
        return gameinfo;
    }

    public void setGameinfo(GameInfo gameinfo) {
        this.gameinfo = gameinfo;
    }

    public PatchNotes getPatch() {
        return patch;
    }

    public void setPatch(PatchNotes patch) {
        this.patch = patch;
    }

    public Boolean getCompress() {
        return compress;
    }

    public void setCompress(Boolean compress) {
        this.compress = compress;
    }

    public Boolean getCache() {
        return cache;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    @Override
    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    public NewsList getNews() {
        return news;
    }

    public void setNews(NewsList news) {
        this.news = news;
    }

    public Integer getPort() {
        return port;
    }

    protected void setPort(Integer port) {
        this.port = port;
    }
}
