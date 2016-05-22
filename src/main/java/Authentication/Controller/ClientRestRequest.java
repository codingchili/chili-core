package Authentication.Controller;

import Authentication.Model.Account;
import Utilities.Serializer;
import Utilities.Token;
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
class ClientRestRequest implements ClientRequest {
    private RoutingContext context;
    private HttpServerResponse response;
    private JsonObject json;
    private Token token;

    ClientRestRequest(RoutingContext context) {
        this.context = context;
        this.response = context.response();

        if (context.request().method().equals(HttpMethod.POST)) {
            this.json = context.getBodyAsJson();

            if (json.containsKey("token"))
                token = (Token) Serializer.unpack(json.getJsonObject("token"), Token.class);
        }
    }

    @Override
    public String realm() {
        return json.getString("realm");
    }

    @Override
    public String account() {
        return token.getDomain();
    }

    @Override
    public String character() {
        return json.getString("character");
    }

    @Override
    public String className() {
        return json.getString("className");
    }

    @Override
    public String sender() {
        MultiMap headers = context.request().headers();

        if (headers.contains("X-Real-IP"))
            return headers.get("X-Real-IP");
        else return context.request().remoteAddress().host();
    }

    @Override
    public Token token() {
        return token;
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
    public void accept() {
        sendStatus(HttpResponseStatus.OK);
    }

    @Override
    public void error() {
        sendStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public Account getAccount() {
        return (Account) Serializer.unpack(json.getJsonObject("account"), Account.class);
    }
}
