package com.codingchili.logging.configuration;

import io.vertx.core.Vertx;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;

import com.codingchili.core.context.ServiceContext;

import static com.codingchili.logging.configuration.LogServerSettings.PATH_LOGSERVER;

/**
 * @author Robin Duda
 */
public class LogContext extends ServiceContext {

    public LogContext(Vertx vertx) {
        super(vertx);
    }

    public LogServerSettings service() {
        return Configurations.get(PATH_LOGSERVER, LogServerSettings.class);
    }

    public boolean consoleEnabled() {
        return service().getConsole();
    }

    public boolean verifyToken(Token token) {
        return new TokenFactory(service().getSecret()).verifyToken(token);
    }

    public boolean elasticEnabled() {
        return service().getElastic().getEnabled();
    }

    public ElasticSettings elasticSettings() {
        return service().getElastic();
    }
}
