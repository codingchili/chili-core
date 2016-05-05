package Configuration;

import Utilities.RemoteAuthentication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Robin on 2016-05-05.
 */
@JsonIgnoreProperties({"path", "name"})
public class WebServerSettings implements Configurable {
    protected static final String WEBSERVER_PATH = "conf/system/webserver.json";
    private RemoteAuthentication logserver;
    private Integer port;

    @Override
    public String getPath() {
        return WEBSERVER_PATH;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
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
}
