package com.codingchili.core.Logging.Model;

import com.codingchili.core.Authentication.Model.Account;
import com.codingchili.core.Configuration.RemoteAuthentication;
import com.codingchili.core.Configuration.VertxSettings;
import com.codingchili.core.Protocols.Util.Serializer;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.codingchili.core.Configuration.Strings.*;

/**
 * @author Robin Duda
 *         Default logging implementation.
 */
public class DefaultLogger extends Handler implements Logger {
    private ConsoleLogger console = new ConsoleLogger();
    private RemoteAuthentication authentication;
    private Vertx vertx;

    public DefaultLogger() {
    }

    public DefaultLogger(Vertx vertx, RemoteAuthentication authentication) {
        this.authentication = authentication;
        this.vertx = vertx;

        vertx.eventBus().consumer(LOCAL_LOGGING, message -> {
            vertx.eventBus().send(NODE_LOGGING, message.body(), new DeliveryOptions().setSendTimeout(10000));
        });
    }

    private void log(JsonObject json) {
        vertx.eventBus().send(LOCAL_LOGGING, json.encode());
        console.log(withoutToken(json));
    }

    private JsonObject event(String name) {
        return event(name, LOG_LEVEL_INFO);
    }

    private JsonObject event(String name, String level) {
        JsonObject event = new JsonObject()
                .put(PROTOCOL_ACTION, PROTOCOL_LOGGING)
                .put(LOG_EVENT, name)
                .put(LOG_LEVEL, level)
                .put(LOG_TIME, Instant.now().toEpochMilli());

        if (authentication != null) {
            event.put(LOG_HOST, authentication.getHost())
                    .put(LOG_SYSTEM, authentication.getSystem())
                    .put(ID_TOKEN, Serializer.json(authentication.getToken()));
        }
        return event;
    }

    @Override
    public void onServerStarted(Future<Void> future) {
        log(event(LOG_SERVER_START, LOG_LEVEL_STARTUP));

        if (vertx.isMetricsEnabled()) {
            MetricsService metricsService = MetricsService.create(vertx);

            vertx.setPeriodic(VertxSettings.METRIC_RATE, handler -> {
                JsonObject metrics = metricsService.getMetricsSnapshot(vertx);
                onMetricsSnapshot(metrics);
            });
        }

        future.complete();
    }

    @Override
    public void onServerStopped(Future<Void> future) {
        log(event(LOG_SERVER_STOP, LOG_LEVEL_SEVERE));

        vertx.setTimer(1000, shutdown -> {
           future.complete();
        });
    }

    private JsonObject withoutToken(JsonObject message) {
        JsonObject clean = message.copy();
        clean.remove(ID_TOKEN);
        return clean;
    }

    @Override
    public void onInstanceStarted(RealmSettings realm, InstanceSettings instance) {
        log(event(LOG_INSTANCE_START, LOG_LEVEL_STARTUP)
                .put(LOG_INSTANCE, instance.getName())
                .put(ID_REALM, realm.getName()));
    }

    @Override
    public void onRealmStarted(RealmSettings realm) {
        log(event(LOG_REALM_START, LOG_LEVEL_STARTUP)
                .put(ID_REALM, realm.getName()));
    }

    @Override
    public void onAuthenticationFailure(Account account, String host) {
        log(event(LOG_ACCOUNT_UNAUTHORIZED, LOG_LEVEL_WARNING)
                .put(DB_USER, account.getUsername())
                .put(LOG_REMOTE, host));
    }

    @Override
    public void onAuthenticated(Account account, String host) {
        log(event(LOG_ACCOUNT_AUTHENTICATED)
                .put(DB_USER, account.getUsername())
                .put(LOG_REMOTE, host));
    }

    @Override
    public void onRegistered(Account account, String host) {
        log(event(LOG_ACCOUNT_REGISTERED)
                .put(DB_USER, account.getUsername())
                .put(LOG_REMOTE, host));
    }

    @Override
    public void onRealmRegistered(RealmSettings realm) {
        log(event(LOG_REALM_REGISTERED)
                .put(ID_REALM, realm.getName()));
    }

    @Override
    public void onRealmDisconnect(String realm) {
        log(event(LOG_REALM_DISCONNECT, LOG_LEVEL_SEVERE)
                .put(ID_REALM, realm));
    }

    @Override
    public void onRealmUpdated(RealmSettings realm) {
        log(event(LOG_REALM_UPDATE)
                .put(ID_REALM, realm.getName())
                .put(ID_PLAYERS, realm.getPlayers()));
    }

    @Override
    public void onRealmRejected(RealmSettings realm) {
        log(event(LOG_REALM_REJECTED, LOG_LEVEL_WARNING)
                .put(ID_REALM, realm.getName()));
    }

    @Override
    public void onPageLoaded(HttpServerRequest request) {
        log(event(LOG_PAGE_LOAD)
                .put(LOG_AGENT, request.getHeader(LOG_USER_AGENT))
                .put(LOG_ORIGIN, request.remoteAddress().host()));
    }

    @Override
    public void patchReloading(String name, String version) {
        log(event(LOG_PATCHER_RELOAD)
                .put(ID_NAME, name)
                .put(LOG_VERSION, version));
    }

    @Override
    public void patchReloaded(String name, String version) {
        log(event(LOG_PATCHER_RELOADED)
                .put(ID_NAME, name)
                .put(LOG_VERSION, version));
    }

    @Override
    public void patchLoaded(String name, String version) {
        log(event(LOG_PATCHER_LOADED)
                .put(ID_NAME, name)
                .put(LOG_VERSION, version));
    }

    @Override
    public void onDatabaseError() {
        log(event(LOG_DATABASE_ERROR, LOG_LEVEL_SEVERE)
                .put(LOG_MESSAGE, LOG_CONNECTION_ERROR));
    }

    @Override
    public void onFileLoadError(String fileName) {
        log(event(LOG_FILE_ERROR, LOG_LEVEL_SEVERE)
                .put(LOG_MESSAGE, fileName));
    }

    @Override
    public void onMetricsSnapshot(JsonObject metrics) {
        log(event(LOG_METRICS)
                .put(ID_DATA, metrics));
    }

    @Override
    public void onHandlerMissing(String action) {
        log(event(LOG_HANDLER_MISSING, LOG_LEVEL_WARNING)
                .put(LOG_MESSAGE, action));
    }

    @Override
    public void publish(LogRecord record) {
        console.log(addTrace(event(LOG_VERTX, record.getLevel().getName()), record)
                .put(LOG_MESSAGE, record.getMessage()));
    }

    private JsonObject addTrace(JsonObject log, LogRecord record) {

        if (record.getThrown() != null) {
            StackTraceElement element = record.getThrown().getStackTrace()[0];

            if (element != null)
                log.put(LOG_TRACE,
                        element.getClassName() + "." +
                                element.getMethodName() + " (" +
                                element.getLineNumber() + ")"
                );

            record.getThrown().printStackTrace();
        }

        return log;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
