package Configuration;

import Utilities.RemoteAuthentication;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Robin on 2016-05-05.
 */
@JsonIgnoreProperties({"path", "name"})
public class LogServerSettings implements Configurable {
    protected static final String LOGSERVER_PATH = "conf/system/logserver.json";
    private RemoteAuthentication logserver;
    private ElasticSettings elastic;
    private byte[] secret;
    private Integer port;


    @Override
    public String getPath() {
        return LOGSERVER_PATH;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }

    public Integer getPort() {
        return port;
    }

    protected void setPort(Integer port) {
        this.port = port;
    }

    public byte[] getSecret() {
        return secret;
    }

    protected void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public ElasticSettings getElastic() {
        return elastic;
    }

    protected void setElastic(ElasticSettings elastic) {
        this.elastic = elastic;
    }

    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }
}