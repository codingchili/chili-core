package com.codingchili.services.Logging.Configuration;

import com.codingchili.core.Configuration.ServiceConfigurable;
import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 *         Contains settings for the logserver.
 */
public class LogServerSettings extends ServiceConfigurable {
    public static final String PATH_LOGSERVER = Strings.getService("logserver");
    private ElasticSettings elastic = new ElasticSettings();
    private byte[] secret = new byte[] {0x53,0x48};
    private Boolean console = true;

    public Boolean getConsole() {
        return console;
    }

    public void setConsole(Boolean console) {
        this.console = console;
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
}