package Utilities;

import Authentication.Model.Account;
import Configuration.Config;
import Game.Model.InstanceSettings;
import Game.Model.RealmSettings;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-07.
 */
public class DefaultLogger implements Logger {
    private ArrayList<String> buffer = new ArrayList<>();
    private boolean connected = false;
    private Vertx vertx;
    private String server;

    public DefaultLogger(Vertx vertx, String tag) {
        this.vertx = vertx;
        this.server = tag;

        vertx.createHttpClient().websocket(Config.Logging.PORT, Config.Logging.REMOTE, "/", handler -> {
            if (!connected) {
                connected = true;

                for (String message : buffer)
                    handler.write(Buffer.buffer(message));

                buffer.clear();
            }

            vertx.eventBus().consumer(Config.Address.LOGS, message -> {
                handler.write(Buffer.buffer(message.body().toString()));
            });

        });
    }

    private void log(JsonObject json) {
        log(json
                .put("server", Config.Logging.NAME)
                .put("system", server)
                .put("time", Instant.now().getEpochSecond())
                .encodePrettily());
    }

    @Override
    public void onServerStarted() {
        log(event("server-started"));
    }

    @Override
    public void onServerStopped() {
        log(event("server-stopped"));
    }

    @Override
    public void onInstanceStarted(InstanceSettings instance) {
        log(event("instance-start")
                .put("name", instance.getName()));
    }

    @Override
    public void onRealmStarted(RealmSettings realm) {
        log(event("realm-start")
                .put("name", realm.getName()));
    }

    @Override
    public void onAuthenticationFailure(Account account, String host) {
        log(event("account-failure")
                .put("username", account.getUsername())
                .put("remote", host));
    }

    @Override
    public void onAuthenticated(Account account, String host) {
        log(event("account-authenticated")
                .put("attempt", "success")
                .put("username", account.getUsername())
                .put("remote", host));
    }

    @Override
    public void onRegistered(Account account, String host) {
        log(event("account-registered")
                .put("username", account.getUsername())
                .put("remote", host));
    }

    @Override
    public void onRealmRegistered(RealmSettings realm) {
        log(event("realm-registered")
                .put("name", realm.getName()));
    }

    @Override
    public void onRealmDeregistered(RealmSettings realm) {
        log(event("realm-deregistered")
                .put("name", realm.getName()));
    }

    @Override
    public void onRealmUpdated(RealmSettings realm) {
        log(event("realm-update")
                .put("name", realm.getName()));
    }

    private JsonObject event(String name) {
        return new JsonObject().put("event", name);
    }

    private void log(String message) {
        if (!connected)
            buffer.add(message);
        else
            vertx.eventBus().send(Config.Address.LOGS, message);
    }
}
