package Configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Robin Duda
 *         Contains settings for the logserver.
 */
@JsonIgnoreProperties({"path", "name"})
public class LogServerSettings implements Configurable {
    static final String LOGSERVER_PATH = "conf/system/logserver.json";
    private RemoteAuthentication logserver;
    private ElasticSettings elastic;
    private byte[] secret;
    private Integer port;
    private Boolean console;


    @Override
    public String getPath() {
        return LOGSERVER_PATH;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }

    public Boolean getConsole() {
        return console;
    }

    public void setConsole(Boolean console) {
        this.console = console;
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

    void setSecret(byte[] secret) {
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