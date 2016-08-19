package Logging.Model;

import Authentication.Model.Account;
import Configuration.*;
import Realm.Configuration.InstanceSettings;
import Realm.Configuration.RealmSettings;
import Protocols.Serializer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Default logging implementation.
 */
public class DefaultLogger implements Logger {
    private ArrayList<String> buffer = new ArrayList<>();
    private RemoteAuthentication authentication;
    private boolean connected = false;
    private Vertx vertx;

    public DefaultLogger(Vertx vertx, RemoteAuthentication authentication) {
        this.authentication = authentication;
        this.vertx = vertx;

        vertx.createHttpClient().websocket(authentication.getPort(), authentication.getRemote(), Strings.DIR_SEPARATOR, handler -> {
            if (!connected) {
                connected = true;

                for (String message : buffer)
                    handler.write(Buffer.buffer(message));

                buffer.clear();
            }

            vertx.eventBus().consumer(ConfigurationLoader.Address.LOGS, message -> {
                handler.write(Buffer.buffer(message.body().toString()));
            });

        });
    }

    private void log(JsonObject json) {
        log(json
                .put(Strings.LOG_HOST, authentication.getHost())
                .put(Strings.LOG_SYSTEM, authentication.getSystem())
                .put(Strings.LOG_TIME, Instant.now().toEpochMilli())
                .put(Strings.ID_TOKEN, Serializer.json(authentication.getToken()))
                .encodePrettily());
    }

    @Override
    public void onServerStarted() {
        log(event(Strings.LOG_SERVER_START));
    }

    @Override
    public void onServerStopped() {
        log(event(Strings.LOG_SERVER_STOP));
    }

    @Override
    public void onInstanceStarted(RealmSettings realm, InstanceSettings instance) {
        log(event(Strings.LOG_INSTANCE_START)
                .put(Strings.LOG_INSTANCE, instance.getName())
                .put(Strings.ID_REALM, realm.getName()));
    }

    @Override
    public void onRealmStarted(RealmSettings realm) {
        log(event(Strings.LOG_REALM_START)
                .put(Strings.ID_REALM, realm.getName()));
    }

    @Override
    public void onAuthenticationFailure(Account account, String host) {
        log(event(Strings.LOG_ACCOUNT_FAILURE)
                .put(Strings.DB_USER, account.getUsername())
                .put(Strings.LOG_REMOTE, host));
    }

    @Override
    public void onAuthenticated(Account account, String host) {
        log(event(Strings.LOG_ACCOUNT_AUTHENTICATED)
                .put(Strings.DB_USER, account.getUsername())
                .put(Strings.LOG_REMOTE, host));
    }

    @Override
    public void onRegistered(Account account, String host) {
        log(event(Strings.LOG_ACCOUNT_REGISTERED)
                .put(Strings.DB_USER, account.getUsername())
                .put(Strings.LOG_REMOTE, host));
    }

    @Override
    public void onRealmRegistered(RealmSettings realm) {
        log(event(Strings.LOG_REALM_REGISTERED)
                .put(Strings.ID_REALM, realm.getName()));
    }

    @Override
    public void onRealmDeregistered(RealmSettings realm) {
        log(event(Strings.LOG_REALM_DEREGISTERED)
                .put(Strings.ID_REALM, realm.getName()));
    }

    @Override
    public void onRealmUpdated(RealmSettings realm) {
        log(event(Strings.LOG_REALM_UPDATE)
                .put(Strings.ID_REALM, realm.getName())
                .put(Strings.ID_PLAYERS, realm.getPlayers()));
    }

    @Override
    public void onRealmRejected(RealmSettings realm) {
        log(event(Strings.LOG_REALM_REJECTED)
                .put(Strings.ID_REALM, realm.getName()));
    }

    @Override
    public void onPageLoaded(HttpServerRequest request) {
        log(event(Strings.LOG_PAGE_LOAD)
                .put(Strings.LOG_AGENT, request.getHeader(Strings.LOG_USER_AGENT))
                .put(Strings.LOG_ORIGIN, request.remoteAddress().host()));
    }

    @Override
    public void patchReloading(String name, String version) {
        log(event(Strings.LOG_PATCHER_RELOAD)
                .put(Strings.ID_NAME, name)
                .put(Strings.LOG_VERSION, version));
    }

    @Override
    public void patchReloaded(String name, String version) {
        log(event(Strings.LOG_PATCHER_RELOADED)
                .put(Strings.ID_NAME, name)
                .put(Strings.LOG_VERSION, version));
    }

    @Override
    public void patchLoaded(String name, String version) {
        log(event(Strings.LOG_PATCHER_LOADED)
                .put(Strings.ID_NAME, name)
                .put(Strings.LOG_VERSION, version));
    }

    private JsonObject event(String name) {
        return new JsonObject().put(Strings.LOG_EVENT, name);
    }

    private void log(String message) {
        if (!connected)
            buffer.add(message);
        else
            vertx.eventBus().send(ConfigurationLoader.Address.LOGS, message);
    }
}
