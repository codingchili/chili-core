package Patching.Controller.Transport;

import Configuration.Strings;
import Patching.Controller.ClientRequest;
import Patching.Model.PatchFile;
import Protocols.Authorization.Token;
import Protocols.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import java.net.URLConnection;

/**
 * @author Robin Duda
 */
class ClientRestRequest implements ClientRequest {
    private RoutingContext context;
    private String action;

    ClientRestRequest(RoutingContext context, String method) {
        this.context = context;
        this.action = method;
    }

    @Override
    public void error() {
        sendStatus(500);
    }

    private void sendStatus(int code) {
        context.response().setStatusCode(code).end();
    }

    @Override
    public void unauthorized() {
        sendStatus(401);
    }

    @Override
    public void write(Object object) {
        context.response().end(Serializer.pack(object));
    }

    @Override
    public void accept() {
        context.response().end();
    }

    @Override
    public void missing() {
        sendStatus(404);
    }

    @Override
    public void conflict() {
        sendStatus(409);
    }

    @Override
    public String action() {
        return action;
    }

    @Override
    public Token token() {
        return null;
    }

    @Override
    public void file(PatchFile file) {
        context.response()
                .putHeader("content-type", getContentType(file))
                .putHeader("content-encoding", "gzip")
                .end(Buffer.buffer(file.getBytes()));
    }

    private String getContentType(PatchFile file) {
        String type = URLConnection.guessContentTypeFromName(file.getPath());

        if (type == null) {
            String filename = file.getPath();
            String extension = filename.substring(filename.lastIndexOf('.'), filename.length());

            switch (extension) {
                case Strings.EXT_JSON:
                    return "application/json";
                case Strings.EXT_SVG:
                    return "image/svg+xml";
                default:
                    return "unknown/type";
            }
        } else {
            return type;
        }
    }

    @Override
    public String file() {
        return context.request().getParam(Strings.ID_FILE);
    }

    @Override
    public String version() {
        String version = context.request().getParam(Strings.LOG_VERSION);

        if (version == null) {
            version = Strings.PATCH_MAX_VERSION;
        }

        return version;
    }
}
