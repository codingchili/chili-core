package com.codingchili.core.Realm.Instance.Controller;

import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Access;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.TokenFactory;
import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Configuration.InstanceProvider;
import com.codingchili.core.Realm.Instance.Configuration.InstanceSettings;
import io.vertx.core.Future;

import static com.codingchili.core.Configuration.Strings.ID_PING;

/**
 * @author Robin Duda
 *         Handles players in a get.
 */
public class InstanceHandler extends AbstractHandler {
    private final Protocol<RequestHandler<InstanceRequest>> protocol = new Protocol<>();
    private final TokenFactory tokens;
    private final InstanceSettings instance;
    private final RealmSettings realm;


    public InstanceHandler(InstanceProvider provider) {
        super(provider.getAddress());
        this.logger = provider.getLogger();
        this.tokens = provider.getTokenFactory();
        this.realm = provider.getRealm();
        this.instance = provider.getInstance();

        protocol.use(ID_PING, this::ping, Access.PUBLIC);
    }

    private void ping(InstanceRequest request) {
        request.accept();
    }

    private Access authenticator(Request request) {
        if (tokens.verifyToken(request.token())) {
            return Access.AUTHORIZED;
        } else {
            return Access.PUBLIC;
        }
    }

    @Override
    public void handle(Request request) throws ProtocolException {
        protocol.get(authenticator(request), request.action()).handle(new InstanceRequest(request));
    }

    @Override
    public void stop(Future<Void> future) {
        logger.onInstanceStopped(future, realm, instance);
    }

    @Override
    public void start(Future<Void> future) {
        logger.onInstanceStarted(realm, instance);
        future.complete();
    }
}
