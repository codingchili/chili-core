package com.codingchili.core.Logging;

import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

/**
 * Authentication.Mock implementation of a logger to disable logging.
 */
public class LoggerMock implements Logger {

    @Override
    public void onServerStarted(Future<Void> future) {
        future.complete();
    }

    @Override
    public void onServerStopped(Future<Void> future) {
        //future.complete();
    }

    @Override
    public void onInstanceStarted(RealmSettings realm, InstanceSettings instance) {

    }

    @Override
    public void onInstanceStopped(Future<Void> future, RealmSettings realm, InstanceSettings instance) {

    }

    @Override
    public void onRealmStarted(RealmSettings realm) {

    }

    @Override
    public void onRealmStopped(Future<Void> future, RealmSettings realm) {

    }

    @Override
    public void onAuthenticationFailure(Account account, String host) {

    }

    @Override
    public void onAuthenticated(Account account, String host) {

    }

    @Override
    public void onRegistered(Account account, String host) {

    }

    @Override
    public void onRealmRegistered(RealmSettings realm) {

    }

    @Override
    public void onRealmDisconnect(String realm) {

    }

    @Override
    public void onRealmUpdated(RealmSettings realm) {

    }

    @Override
    public void onRealmRejected(RealmSettings realm) {

    }

    @Override
    public void onPageLoaded(HttpServerRequest request) {

    }

    @Override
    public void onPatchReloading(String name, String version) {

    }

    @Override
    public void onPatchReloaded(String name, String version) {

    }

    @Override
    public void onPatchLoaded(String name, String version) {

    }

    @Override
    public void onFileLoadError(String fileName) {

    }

    @Override
    public void onMetricsSnapshot(JsonObject metrics) {

    }

    @Override
    public void onHandlerMissing(String action) {

    }

    @Override
    public void onDeployRealmFailure(RealmSettings realm) {

    }

    @Override
    public void onFileLoaded(String loader, String path) {

    }

    @Override
    public void onCacheCleared(String component) {

    }

    @Override
    public void onFileSaved(String component, String path) {

    }
}
