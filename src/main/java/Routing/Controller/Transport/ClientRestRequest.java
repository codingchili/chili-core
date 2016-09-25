package Routing.Controller.Transport;

import Authentication.Controller.ClientAuthenticationRequest;
import Authentication.Model.Account;
import Configuration.Strings;
import Protocols.Authentication.ClientAuthentication;
import Protocols.Authorization.Token;
import Protocols.Serializer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Robin Duda
 */

class ClientRestRequest implements ClientAuthenticationRequest {
    private RoutingContext context;
    private HttpServerResponse response;
    private JsonObject json;
    private Token token;

    ClientRestRequest(RoutingContext context) {
        this.context = context;
        this.response = context.response();

        if (context.request().method().equals(HttpMethod.POST)) {
            this.json = context.getBodyAsJson();

            if (json.containsKey(Strings.ID_TOKEN))
                token = Serializer.unpack(json.getJsonObject(Strings.ID_TOKEN), Token.class);
        }
    }

    @Override
    public String realmName() {
        return json.getString(Strings.ID_REALM);
    }

    @Override
    public String account() {
        return token.getDomain();
    }

    @Override
    public String character() {
        return json.getString(Strings.ID_CHARACTER);
    }

    @Override
    public String className() {
        return json.getString(Strings.PROTOCOL_CLASS_NAME);
    }

    @Override
    public String sender() {
        MultiMap headers = context.request().headers();

        if (headers.contains(Strings.PROTOCOL_REAL_IP))
            return headers.get(Strings.PROTOCOL_REAL_IP);
        else
            return context.request().remoteAddress().host();
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public JsonObject data() {
        return null;
    }

    @Override
    public int timeout() {
        return 0;
    }

    @Override
    public void write(Object object) {
        try {
            response.end(Buffer.buffer(Serializer.pack(object)));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unauthorized() {
        sendStatus(HttpResponseStatus.UNAUTHORIZED);
    }

    private void sendStatus(HttpResponseStatus status) {
        response.setStatusCode(status.code()).end();
    }

    @Override
    public void missing() {
        sendStatus(HttpResponseStatus.NOT_FOUND);
    }

    @Override
    public void conflict() {
        sendStatus(HttpResponseStatus.CONFLICT);
    }

    @Override
    public String action() {
        return context.request().path().replace("/api/", "");
    }

    @Override
    public String target() {
        return null;
    }

    @Override
    public void accept() {
        sendStatus(HttpResponseStatus.OK);
    }

    @Override
    public void error() {
        sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Account getAccount() {
        return Serializer.unpack(json.getJsonObject(Strings.ID_ACCOUNT), Account.class);
    }

    @Override
    public void authenticate(ClientAuthentication authentication) {
        response.end(Serializer.pack(authentication));
    }
}
