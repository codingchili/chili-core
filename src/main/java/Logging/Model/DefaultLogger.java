package Logging.Model;

import Authentication.Model.Account;
import Configuration.ConfigurationLoader;
import Configuration.RemoteAuthentication;
import Configuration.Strings;
import Configuration.VertxSettings;
import Protocols.Serializer;
import Realm.Configuration.InstanceSettings;
import Realm.Configuration.RealmSettings;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.MetricsService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Robin Duda
 *         Default logging implementation.
 */
public class DefaultLogger extends Handler implements Logger {
    private static final int CLOSE_TIMEOUT = 1500;
    private ConsoleLogger console = new ConsoleLogger();
    private ArrayList<String> buffer = new ArrayList<>();
    private RemoteAuthentication authentication;
    private boolean connected = false;
    private Vertx vertx;

    public DefaultLogger() {
    }

    public DefaultLogger(Vertx vertx, RemoteAuthentication authentication) {
        this.authentication = authentication;
        this.vertx = vertx;

        vertx.createHttpClient().websocket(authentication.getPort(), authentication.getRemote(), Strings.DIR_SEPARATOR, handler -> {
            if (!connected) {
                connected = true;

                for (String message : buffer)
                    handler.write(Buffer.buffer(message));
            }

            vertx.eventBus().consumer(ConfigurationLoader.Address.LOGS, message -> {
                handler.write(Buffer.buffer(message.body().toString()));
            });
        });
    }

    private void log(JsonObject json) {
        log(json.encodePrettily());
    }

    private JsonObject event(String name) {
        return event(name, Strings.LOG_LEVEL_INFO);
    }

    private JsonObject event(String name, String level) {
        JsonObject event = new JsonObject()
                .put(Strings.LOG_EVENT, name)
                .put(Strings.LOG_LEVEL, level)
                .put(Strings.LOG_TIME, Instant.now().toEpochMilli());

        if (authentication != null) {
            event.put(Strings.LOG_HOST, authentication.getHost())
                    .put(Strings.LOG_SYSTEM, authentication.getSystem())
                    .put(Strings.ID_TOKEN, Serializer.json(authentication.getToken()));
        }
        return event;
    }

    @Override
    public void onServerStarted(Future<Void> future) {
        log(event(Strings.LOG_SERVER_START, Strings.LOG_LEVEL_STARTUP));

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
        JsonObject message = event(Strings.LOG_SERVER_STOP, Strings.LOG_LEVEL_SEVERE);

        log(message);
        console.log(withoutToken(message));

        vertx.setTimer(CLOSE_TIMEOUT, handler -> {
            future.complete();
        });
    }

    private JsonObject withoutToken(JsonObject message) {
        message.remove(Strings.ID_TOKEN);
        return message;
    }

    @Override
    public void onInstanceStarted(RealmSettings realm, InstanceSettings instance) {
        log(event(Strings.LOG_INSTANCE_START, Strings.LOG_LEVEL_STARTUP)
                .put(Strings.LOG_INSTANCE, instance.getName())
                .put(Strings.ID_REALM, realm.getName()));
    }

    @Override
    public void onRealmStarted(RealmSettings realm) {
        log(event(Strings.LOG_REALM_START, Strings.LOG_LEVEL_STARTUP)
                .put(Strings.ID_REALM, realm.getName()));
    }

    @Override
    public void onAuthenticationFailure(Account account, String host) {
        log(event(Strings.LOG_ACCOUNT_UNAUTHORIZED, Strings.LOG_LEVEL_WARNING)
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
    public void onRealmDisconnect(String realm) {
        log(event(Strings.LOG_REALM_DISCONNECT, Strings.LOG_LEVEL_SEVERE)
                .put(Strings.ID_REALM, realm));
    }

    @Override
    public void onRealmUpdated(RealmSettings realm) {
        log(event(Strings.LOG_REALM_UPDATE)
                .put(Strings.ID_REALM, realm.getName())
                .put(Strings.ID_PLAYERS, realm.getPlayers()));
    }

    @Override
    public void onRealmRejected(RealmSettings realm) {
        log(event(Strings.LOG_REALM_REJECTED, Strings.LOG_LEVEL_WARNING)
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
        log(event(Strings.LOG_PATCHER_LOADED, Strings.LOG_LEVEL_STARTUP)
                .put(Strings.ID_NAME, name)
                .put(Strings.LOG_VERSION, version));
    }

    @Override
    public void onDatabaseError() {
        log(event(Strings.LOG_DATABASE_ERROR, Strings.LOG_LEVEL_SEVERE)
                .put(Strings.LOG_MESSAGE, Strings.LOG_CONNECTION_ERROR));
    }

    @Override
    public void onFileLoadError(String fileName) {
        log(event(Strings.LOG_FILE_ERROR, Strings.LOG_LEVEL_SEVERE)
                .put(Strings.LOG_MESSAGE, fileName));
    }

    @Override
    public void onMetricsSnapshot(JsonObject metrics) {
        log(event(Strings.LOG_METRICS)
                .put(Strings.ID_DATA, metrics));
    }

    private void log(String message) {
        if (!connected)
            buffer.add(message);
        else
            vertx.eventBus().send(ConfigurationLoader.Address.LOGS, message);
    }

    @Override
    public void publish(LogRecord record) {
        console.log(addTrace(event(Strings.LOG_VERTX, record.getLevel().getName()), record)
                .put(Strings.LOG_MESSAGE, record.getMessage()));
    }

    private JsonObject addTrace(JsonObject log, LogRecord record) {

        if (record.getThrown() != null) {
            StackTraceElement element = record.getThrown().getStackTrace()[0];

            if (element != null)
                log.put(Strings.LOG_TRACE,
                        element.getClassName() + "." +
                                element.getMethodName() + " (" +
                                element.getLineNumber() + ")"
                );
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
