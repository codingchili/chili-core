package com.codingchili.core.Logging.Model;

import com.codingchili.core.Logging.Configuration.ElasticSettings;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 */
public class ElasticLogger {
    private final Vertx vertx;
    private final ElasticSettings settings;

    public ElasticLogger(ElasticSettings settings, Vertx vertx) {
        this.vertx = vertx;
        this.settings = settings;

        if (settings.getEnabled()) {
            createIndex();
        }
    }

    public void log(JsonObject data) {
        if (settings.getEnabled()) {
            vertx.createHttpClient().post(
                    settings.getPort(),
                    settings.getRemote(),
                    settings.getIndex() + "/all/", response -> response.handler(event -> {
                    }))
                    .end(data.encode());
        }
    }


    private void createIndex() {
        vertx.createHttpClient().put(
                settings.getPort(),
                settings.getRemote(),
                settings.getIndex(), connection -> connection.handler(data -> {
                })).end(settings.getTemplate().toString());
    }
}
