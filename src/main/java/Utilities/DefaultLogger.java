package Utilities;

import Authentication.Model.Account;
import Game.Model.InstanceSettings;
import Game.Model.RealmSettings;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Robin on 2016-04-07.
 */
public class DefaultLogger implements Logger {
    private ArrayList<String> buffer = new ArrayList<>();
    private RemoteAuthentication authentication;
    private boolean connected = false;
    private Vertx vertx;

    public DefaultLogger(Vertx vertx, RemoteAuthentication authentication) {
        this.authentication = authentication;
        this.vertx = vertx;

        vertx.createHttpClient().websocket(authentication.getPort(), authentication.getRemote(), "/", handler -> {
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
                .put("host", authentication.getHost())
                .put("system", authentication.getSystem())
                .put("time", Instant.now().toEpochMilli())
                .put("token", Serializer.json(authentication.getToken()))
                .encodePrettily());
    }

    @Override
    public void onServerStarted() {
        log(event("server.started"));
    }

    @Override
    public void onServerStopped() {
        log(event("server.stopped"));
    }

    @Override
    public void onInstanceStarted(InstanceSettings instance) {
        log(event("instance.start")
                .put("name", instance.getName()));
    }

    @Override
    public void onRealmStarted(RealmSettings realm) {
        log(event("realm.start")
                .put("name", realm.getName()));
    }

    @Override
    public void onAuthenticationFailure(Account account, String host) {
        log(event("account.failure")
                .put("username", account.getUsername())
                .put("remote", host));
    }

    @Override
    public void onAuthenticated(Account account, String host) {
        log(event("account.authenticated")
                .put("username", account.getUsername())
                .put("remote", host));
    }

    @Override
    public void onRegistered(Account account, String host) {
        log(event("account.registered")
                .put("username", account.getUsername())
                .put("remote", host));
    }

    @Override
    public void onRealmRegistered(RealmSettings realm) {
        log(event("realm.registered")
                .put("name", realm.getName()));
    }

    @Override
    public void onRealmDeregistered(RealmSettings realm) {
        log(event("realm.deregistered")
                .put("name", realm.getName()));
    }

    @Override
    public void onRealmUpdated(RealmSettings realm) {
        log(event("realm.update")
                .put("name", realm.getName()));
    }

    @Override
    public void onRealmRejected(RealmSettings realm) {
        log(event("realm.rejected")
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
