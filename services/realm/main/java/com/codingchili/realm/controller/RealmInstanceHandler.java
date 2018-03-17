package com.codingchili.realm.controller;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.context.InstanceContext;
import com.codingchili.realm.instance.context.InstanceSettings;
import com.codingchili.realm.instance.controller.InstanceHandler;
import com.codingchili.realm.model.RealmUpdate;
import com.codingchili.realm.model.UpdateResponse;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.*;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.ID_ACCOUNT;
import static com.codingchili.common.Strings.NODE_AUTHENTICATION_REALMS;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * @author Robin Duda
 *
 * Handles messaging between the realm and connected instances.
 */
public class RealmInstanceHandler implements CoreHandler {
    private Protocol<Request> protocol = new Protocol<>(this);
    private RealmContext context;
    private boolean registered = false;

    public RealmInstanceHandler(RealmContext context) {
        this.context = context;
        context.periodic(context::updateRate, getClass().getSimpleName(), this::registerRealm);
    }

    @Override
    public void start(Future<Void> future) {
        context.onRealmStarted(context.realm().getName());
        deployInstances(future);
    }

    @Api
    public void any(Request request) {
        // handles any else request..?
        String receiver = request.data().getString(ID_ACCOUNT);
        Connection connection = context.connections().get(receiver);
        if (connection != null) {
            try {
                connection.write(request.data());
                request.accept();
            } catch (Exception e) {
                context.connections().remove(receiver);
                request.error(e);
            }
        } else {
            request.error(new CoreRuntimeException("Connection with id '" + receiver + "' not available."));
        }
    }

    @Api
    public void save(Request request) {
        request.accept();
    }

    @Api
    public void disconnect(Request request) {
        request.accept();
    }

    private void deployInstances(Future<Void> future) {
        List<Future> futures = new ArrayList<>();
        for (InstanceSettings instance : context.instances()) {
            Future deploy = Future.future();
            futures.add(deploy);

            InstanceHandler handler = new InstanceHandler(new InstanceContext(context, instance));

            // sometime in the future the instances will be deployed remotely - just deploy
            // the instances on the same cluster.
            context.handler(() -> handler).setHandler((done) -> {
                if (done.succeeded()) {
                    deploy.complete();
                } else {
                    context.onInstanceFailed(instance.getName(), done.cause());
                    deploy.fail(done.cause());
                }
            });
        }
        CompositeFuture.all(futures).setHandler(done -> {
            if (done.succeeded()) {
                future.complete();
            } else {
                future.fail(done.cause());
            }
        });
    }


    private void registerRealm(Long handler) {
        RealmUpdate realm = new RealmUpdate(context.realm())
                .setPlayers(context.connections().size() + 1);

        context.bus().send(NODE_AUTHENTICATION_REALMS, Serializer.json(realm), response -> {
            if (response.succeeded()) {
                UpdateResponse update = new UpdateResponse(response.result());

                if (update.is(ACCEPTED)) {
                    if (!registered) {
                        context.onRealmRegistered(context.realm().getName());
                    }
                    registered = true;
                } else {
                    registered = false;
                    context.onRealmRejected(context.realm().getName(), update.message());
                }
            }
        });
    }

    @Override
    public String address() {
        return context.realm().getName();
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route()).submit(request);
    }
}
