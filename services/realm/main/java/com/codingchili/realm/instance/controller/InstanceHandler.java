package com.codingchili.realm.instance.controller;

import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;
import com.codingchili.realm.instance.configuration.InstanceContext;
import io.vertx.core.Future;

import static com.codingchili.common.Strings.ID_PING;

/**
 * @author Robin Duda
 * Handles players in an instance.
 */
public class InstanceHandler implements CoreHandler, DeploymentAware {
    private final Protocol<InstanceRequest> protocol = new Protocol<>();
    private InstanceContext context;

    public InstanceHandler(InstanceContext context) {
        this.context = context;
        protocol.use(ID_PING, this::ping, Role.PUBLIC);
    }

    private void ping(InstanceRequest request) {
        request.accept();
    }

    private Role authenticator(Request request) {
        if (context.verifyToken(request.token())) {
            return Role.USER;
        } else {
            return Role.PUBLIC;
        }
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
        context.onInstanceStopped(future, context.realm().getName(), context.instance().getName());
    }

    @Override
    public void start(Future<Void> future) {
        // todo: set up the game loop
        // todo: set up session handling

        context.onInstanceStarted(context.realm().getName(), context.instance().getName());
        future.complete();
    }

    @Override
    public int instances() {
        return 1;
    }
}
