package com.codingchili.core.Logging.Model;

import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;


/**
 * @author Robin Duda
 *         simplifies the generation of logging messages.
 */
public interface Logger {

    /**
     * Emit when a new server has started.
     */
    void onServerStarted(Future<Void> future);

    /**
     * Emit when a server has stopped.
     */
    void onServerStopped(Future<Void> future);

    /**
     * Emit when a new instance has been created.
     */
    void onInstanceStarted(RealmSettings realm, InstanceSettings instance);

    /**
     * Emit when an instance has stopped.
     */
    void onInstanceStopped(Future<Void> future, RealmSettings realm, InstanceSettings instance);

    /**
     * Emit when a realmName has been started.
     */
    void onRealmStarted(RealmSettings realm);

    /**
     * Emit when a realm has stopped.
     */
    void onRealmStopped(Future<Void> future, RealmSettings realm);

    /**
     * Emit when an authentication failure occured on accounts.
     *
     * @param host that the request originated from.
     */
    void onAuthenticationFailure(Account account, String host);

    /**
     * Emit when an account has been authenticated.
     *
     * @param host the originating host.
     */
    void onAuthenticated(Account account, String host);

    /**
     * Emit when a new character has been register.
     *
     * @param host the originating host.
     */
    void onRegistered(Account account, String host);

    /**
     * Emit when a realmName successfully register with the authentication server.
     */
    void onRealmRegistered(RealmSettings realm);

    /**
     * Emit when a realmName was unregistered/disconnected.
     */
    void onRealmDisconnect(String realm);

    /**
     * Emit when a realmName has updated its status with the authentication server.
     */
    void onRealmUpdated(RealmSettings realm);

    /**
     * Emit when a realmName has been rejected by the authentication server.
     */
    void onRealmRejected(RealmSettings realm);

    /**
     * Emit when the patchserver has served the root page /
     */
    void onPageLoaded(HttpServerRequest request);

    /**
     * Emit when a change in patch files are detected and files have started to reload.
     */
    void patchReloading(String name, String version);

    /**
     * Emit when the reloading of patch files have completed.
     */
    void patchReloaded(String name, String version);

    /**
     * Emit when patch version is loaded.
     */
    void patchLoaded(String name, String version);

    /**
     * Emit when failing to load specified fileName;
     */
    void onFileLoadError(String fileName);

    /**
     * Send METRICS_ENABLED to the logger.
     */
    void onMetricsSnapshot(JsonObject metrics);

    /**
     * Throw when a requested handler was not found.
     *
     * @param action the name of the missing handler.
     */
    void onHandlerMissing(String action);

    /**
     * Emit when a realm has failed deployment.
     */
    void onDeployRealmFailure(RealmSettings realm);
}
