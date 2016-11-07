package com.codingchili.services.Realm.Instance.Controller;

import io.vertx.core.Future;

import com.codingchili.core.Exception.CoreException;
import com.codingchili.core.Protocol.*;

import com.codingchili.services.Realm.Instance.Configuration.InstanceContext;

import static com.codingchili.services.Shared.Strings.ID_PING;

/**
 * @author Robin Duda
 *         Handles players in a get.
 */
public class InstanceHandler<T extends InstanceContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<InstanceRequest>> protocol = new Protocol<>();

    public InstanceHandler(T context) {
        super(context, context.address());

        protocol.use(ID_PING, this::ping, Access.PUBLIC);
    }

    private void ping(InstanceRequest request) {
        request.accept();
    }

    private Access authenticator(Request request) {
        if (context.verifyToken(request.token())) {
            return Access.AUTHORIZED;
        } else {
            return Access.PUBLIC;
        }
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(authenticator(request), request.action()).handle(new InstanceRequest(request));
    }

    @Override
    public void stop(Future<Void> future) {
        context.onInstanceStopped(future, context.realm().getName(), context.instance().getName());
    }

    @Override
    public void start(Future<Void> future) {
        context.onInstanceStarted(context.realm().getName(), context.instance().getName());
        future.complete();
    }
}
