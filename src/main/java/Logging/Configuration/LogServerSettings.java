package Logging.Configuration;

import Configuration.Configurable;
import Configuration.RemoteAuthentication;
import Configuration.Strings;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Robin Duda
 *         Contains settings for the logserver.
 */
@JsonIgnoreProperties({"name", "system", "host"})
public class LogServerSettings implements Configurable {
    private RemoteAuthentication logserver;
    private ElasticSettings elastic;
    private byte[] secret;
    private Integer port;
    private Boolean console;


    @Override
    public String getPath() {
        return Strings.PATH_LOGSERVER;
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

    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public ElasticSettings getElastic() {
        return elastic;
    }

    protected void setElastic(ElasticSettings elastic) {
        this.elastic = elastic;
    }

    @Override
    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

}