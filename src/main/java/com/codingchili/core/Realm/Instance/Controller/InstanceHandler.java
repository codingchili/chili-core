package com.codingchili.core.Realm.Instance.Controller;

import com.codingchili.core.Logging.Model.DefaultLogger;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.core.Realm.Configuration.RealmServerSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 *         Handles players in a get.
 */
public class InstanceHandler implements Verticle {
    private Vertx vertx;
    private InstanceSettings settings;
    private RealmSettings realm;
    private RealmServerSettings game;
    private Logger logger;

    public InstanceHandler(RealmServerSettings game, RealmSettings realm, InstanceSettings settings) {
        this.game = game;
        this.realm = realm;
        this.settings = settings;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.logger = new DefaultLogger(vertx, game.getLogserver());
    }

    @Override
    public void start(Future<Void> start) throws Exception {
        logger.onInstanceStarted(realm, settings);
        start.complete();
    }

    @Override
    public void stop(Future<Void> stop) throws Exception {
        stop.complete();
    }
}
