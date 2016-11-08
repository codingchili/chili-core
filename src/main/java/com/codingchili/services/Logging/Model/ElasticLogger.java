package com.codingchili.services.Logging.Model;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.Logging.*;

import com.codingchili.services.Logging.Configuration.ElasticSettings;
import com.codingchili.services.Logging.Configuration.LogContext;

/**
 * @author Robin Duda
 */
public class ElasticLogger extends DefaultLogger implements JsonLogger {
    private final LogContext context;
    private final Vertx vertx;

    public ElasticLogger(LogContext context) {
        this.context = context;
        this.vertx = context.vertx();

        if (context.elasticEnabled()) {
            createIndex();
        }
    }

    public Logger log(JsonObject data) {
        if (context.elasticEnabled()) {
            ElasticSettings settings = context.elasticSettings();

            vertx.createHttpClient().post(
                    settings.getPort(),
                    settings.getRemote(),
                    settings.getIndex() + "/all/", response -> response.handler(event -> {
                    }))
                    .end(data.encode());
        }
        return this;
    }


    private void createIndex() {
        ElasticSettings settings = context.elasticSettings();

        vertx.createHttpClient().put(
                settings.getPort(),
                settings.getRemote(),
                settings.getIndex(), connection -> connection.handler(data -> {
                })).end(settings.getTemplate().toString());
    }
}