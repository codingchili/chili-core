package Logging;

import Authentication.Model.Account;
import Realm.Configuration.InstanceSettings;
import Realm.Configuration.RealmSettings;
import Logging.Model.Logger;
import io.vertx.core.http.HttpServerRequest;

/**
 * Authentication.Mock implementation of a logger to disable logging.
 */
public class LoggerMock implements Logger {
    @Override
    public void onServerStarted() {

    }

    @Override
    public void onServerStopped() {

    }

    @Override
    public void onInstanceStarted(RealmSettings realm, InstanceSettings instance) {

    }

    @Override
    public void onRealmStarted(RealmSettings realm) {

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
    public void onRealmDeregistered(RealmSettings realm) {

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
    public void patchReloading(String name, String version) {

    }

    @Override
    public void patchReloaded(String name, String version) {

    }

    @Override
    public void patchLoaded(String name, String version) {

    }
}
