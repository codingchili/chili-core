package Utilities;

import Authentication.Model.Account;
import Configuration.InstanceSettings;
import Configuration.RealmSettings;
import io.vertx.core.http.HttpServerRequest;


/**
 * @author Robin Duda
 *         simplifies the generation of logging messages.
 */
public interface Logger {

    /**
     * Emit when a new server has started.
     */
    void onServerStarted();

    /**
     * Emit when a server has stopped.
     */
    void onServerStopped();

    /**
     * Emit when a new instance has been created.
     */
    void onInstanceStarted(RealmSettings realm, InstanceSettings instance);

    /**
     * Emit when a realm has been started.
     */
    void onRealmStarted(RealmSettings realm);

    /**
     * Emit when an authentication failure occured on accounts.
     * @param host that the request originated from.
     */
    void onAuthenticationFailure(Account account, String host);

    /**
     * Emit when an account has been authenticated.
     * @param host the originating host.
     */
    void onAuthenticated(Account account, String host);

    /**
     * Emit when a new character has been register.
     * @param host the originating host.
     */
    void onRegistered(Account account, String host);

    /**
     * Emit when a realm successfully register with the authentication server.
     */
    void onRealmRegistered(RealmSettings realm);

    /**
     * Emit when a realm was unregistered/disconnected.
     */
    void onRealmDeregistered(RealmSettings realm);

    /**
     * Emit when a realm has updated its status with the authentication server.
     */
    void onRealmUpdated(RealmSettings realm);

    /**
     * Emit when a realm has been rejected by the authentication server.
     */
    void onRealmRejected(RealmSettings realm);

    /**
     * Emit when the webserver has served the root page /
     */
    void onPageLoaded(HttpServerRequest request);
}
