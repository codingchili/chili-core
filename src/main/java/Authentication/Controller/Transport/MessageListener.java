package Authentication.Controller.Transport;

import Authentication.Configuration.AuthProvider;
import Protocols.Access;
import Protocols.Authorization.TokenFactory;
import Protocols.Protocol;
import Protocols.Request;
import Protocols.Transport.BusListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import static Protocols.Access.AUTHORIZE;
import static Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 */
public class MessageListener extends AbstractVerticle implements BusListener {
    private Protocol protocol;
    private AuthProvider provider;
    private TokenFactory clientToken;
    private TokenFactory realmToken;

    public MessageListener(AuthProvider provider) {
        this.clientToken = provider.getClientTokenFactory();
        this.realmToken = provider.getRealmTokenFactory();


    }

    private void packet() {

    }

   /* public void handle(Request request) {
        try {
            protocol.get(request.action(), access(request)).handle(request);
        } catch (AuthorizationRequiredException authorizationRequired) {
            request.unauthorized();
        } catch (Exception e) {
            request.error();
        }
    }*/

    private Access access(Request request) {
        boolean authorized = realmToken.verifyToken(request.token());
        return (authorized) ? AUTHORIZE : PUBLIC;
    }

    @Override
    public void start(Future<Void> start) {
        start.complete();
    }
}