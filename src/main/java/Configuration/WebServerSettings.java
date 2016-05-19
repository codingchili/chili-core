package Configuration;

import Utilities.RemoteAuthentication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-05-05.
 */
@JsonIgnoreProperties({"path", "name"})
public class WebServerSettings implements Configurable {
    protected static final String WEBSERVER_PATH = "conf/system/webserver.json";
    private RemoteAuthentication logserver;
    private RemoteAuthentication authserver;
    private PatchNotes patch;
    private ArrayList<NewsItem> news;
    private GameInfo info;
    private Integer port;
    private String keyPath;

    @Override
    public String getPath() {
        return WEBSERVER_PATH;
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

    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    public Integer getPort() {
        return port;
    }

    protected void setPort(Integer port) {
        this.port = port;
    }

    public PatchNotes getPatch() {
        return patch;
    }

    protected void setPatch(PatchNotes patch) {
        this.patch = patch;
    }

    public ArrayList<NewsItem> getNews() {
        return news;
    }

    protected void setNews(ArrayList<NewsItem> news) {
        this.news = news;
    }

    public GameInfo getInfo() {
        return info;
    }

    protected void setInfo(GameInfo info) {
        this.info = info;
    }
}
