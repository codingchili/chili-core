package Configuration.MetaServer;

import Configuration.Configurable;
import Configuration.RemoteAuthentication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains settings for the webserver.
 */
@JsonIgnoreProperties({"path", "name"})
public class MetaServerSettings implements Configurable {
    public static final String METASERVER_PATH = "conf/system/metaserver.json";
    private RemoteAuthentication authserver;
    private RemoteAuthentication logserver;
    private GameInfo gameinfo;
    private PatchNotes patch;
    private ArrayList<NewsItem> news;
    private Integer port;
    private Boolean cache;
    private Boolean compress;

    @Override
    public String getPath() {
        return METASERVER_PATH;
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

    public ArrayList<NewsItem> getNews() {
        return news;
    }

    public void setNews(ArrayList<NewsItem> news) {
        this.news = news;
    }

    public Integer getPort() {
        return port;
    }

    protected void setPort(Integer port) {
        this.port = port;
    }
}
