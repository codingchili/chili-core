package Routing.Controller.Transport;

import Authentication.Configuration.AuthProvider;
import Authentication.Controller.ClientHandler;
import Authentication.Controller.RealmHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
class MessageListener extends AbstractVerticle {
    private ClientHandler clientHandler;
    private RealmHandler realmHandler;
    private AuthProvider provider;

    public MessageListener(AuthProvider provider) {
        clientHandler = new ClientHandler(provider);
        realmHandler = new RealmHandler(provider);
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