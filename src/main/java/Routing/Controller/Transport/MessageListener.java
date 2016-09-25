package Routing.Controller.Transport;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.ClientAuthenticationHandler;
import Authentication.Controller.RealmAuthenticationHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
class MessageListener extends AbstractVerticle {
    private ClientAuthenticationHandler clientHandler;
    private RealmAuthenticationHandler realmAuthenticationHandler;
    private AuthProvider provider;

    public MessageListener(AuthProvider provider) {
        clientHandler = new ClientAuthenticationHandler(provider);
        realmAuthenticationHandler = new RealmAuthenticationHandler(provider);
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


    @Override
    public void start(Future<Void> start) {
        start.complete();
    }
}