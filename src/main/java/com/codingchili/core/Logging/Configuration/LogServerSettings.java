package com.codingchili.core.Logging.Configuration;

import com.codingchili.core.Configuration.Configurable;
import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Configuration.Strings;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Robin Duda
 *         Contains settings for the logserver.
 */
@JsonIgnoreProperties({"system", "host"})
public class LogServerSettings implements Configurable {
    private RemoteAuthentication logserver;
    private ElasticSettings elastic;
    private byte[] secret;
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

    public byte[] getSecret() {
        return secret;
    }

    public LogServerSettings setSecret(byte[] secret) {
        this.secret = secret;
        return this;
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