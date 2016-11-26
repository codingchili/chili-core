package com.codingchili.services.authentication.model;

import io.vertx.core.Future;

import java.time.Instant;
import java.util.ArrayList;

import com.codingchili.services.authentication.configuration.AuthContext;
import com.codingchili.services.realm.configuration.RealmSettings;

/**
 * @author Robin Duda
 */
public class StaleRealmHandler {
    private static StaleRealmHandler instance;
    private final AuthContext context;
    private final AsyncRealmStore realms;

    public static void watch(AuthContext context, AsyncRealmStore realms) {
        if (instance == null) {
            instance = new StaleRealmHandler(context, realms);
        }
    }

    public static void stop() {
        instance = null;
    }

    private StaleRealmHandler(AuthContext context, AsyncRealmStore realms) {
        this.context = context;
        this.realms = realms;

        context.periodic(context::realmTimeout, getClass().getSimpleName(), this::clearStaleRealms);
    }

    private void clearStaleRealms(Long handler) {
        Future<ArrayList<RealmMetaData>> list = Future.future();

        list.setHandler(result -> {
            if (result.succeeded()) {

                result.result().stream()
                        .filter(this::isRealmStale)
                        .forEach(realm -> doRemove(realm.getName()));
            } else {
                context.onStaleClearError(result.cause());
            }
        });

        realms.getMetadataList(list);
    }

    private void doRemove(String realm) {
        Future<RealmSettings> future = Future.future();

        future.setHandler(done -> {
            if (done.succeeded()) {
                if (done.result() != null) {
                    context.onRealmDisconnect(done.result().getName());
                }
            } else {
                context.onStaleRemoveError(done.cause());
            }
        });
        realms.remove(future, realm);
    }

    private boolean isRealmStale(RealmMetaData realm) {
        return (realm.getUpdated() < Instant.now().toEpochMilli() - context.realmTimeout());
    }
}
