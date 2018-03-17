package com.codingchili.realm.instance.controller;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.context.InstanceContext;
import io.vertx.core.Future;

import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;

/**
 * @author Robin Duda
 * Handles players in an settings.
 */
public class InstanceHandler implements CoreHandler, DeploymentAware {
    private final Protocol<Request> protocol = new Protocol<>(this);
    private InstanceContext context;

    public InstanceHandler(InstanceContext context) {
        this.context = context;

        GameContext game = new GameContext(context);

        protocol.annotated(new MovementHandler(game));
        protocol.annotated(new TradeHandler(game));
        protocol.annotated(new SpellHandler(game));
        protocol.annotated(new DialogHandler(game));
    }

    @Api
    public void ping(InstanceRequest request) {
        request.accept();
    }

    private Role authenticator(Request request) {
        if (context.verifyToken(request.token())) {
            return Role.USER;
        } else {
            return Role.PUBLIC;
        }
    }

    @Api
    public void joinInstance(InstanceRequest request) {

        request.accept();
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route(), authenticator(request)).submit(new InstanceRequest(request));
    }

    @Override
    public String address() {
        return context.address();
    }

    @Override
    public void stop(Future<Void> future) {
        context.onInstanceStopped(future, context.realm().getName(), context.settings().getName());
    }

    @Override
    public void start(Future<Void> future) {
        //context.onInstanceStarted(context.realm().getName(), context.settings().getName());
        future.complete();
    }

    // todo: only log listener started if handler does not implement start or ignore it altogether?
    // todo: instance metadata is wrong in the listener.start logging, fix.
    // todo: log loaded X instances, X realms, X classes, X afflictions, X spells - once in the static loader.

    @Override
    public int instances() {
        return 1;
    }
}
