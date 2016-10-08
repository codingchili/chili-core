package com.codingchili.core.Routing.Controller.Transport;

import com.codingchili.core.Authentication.Configuration.AuthProvider;
import com.codingchili.core.Authentication.Controller.ClientHandler;
import com.codingchili.core.Authentication.Controller.RealmHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 */
class MessageListener extends AbstractVerticle {
    private ClientHandler clientHandler;
    private RealmHandler realmAuthenticationHandler;
    private AuthProvider provider;

    public MessageListener(AuthProvider provider) {
        clientHandler = new ClientHandler(provider);
        realmAuthenticationHandler = new RealmHandler(provider);
    }

    private void packet() {

    }

   /* public void get(Request request) {
        try {
            protocol.get(request.action(), access(request)).get(request);
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