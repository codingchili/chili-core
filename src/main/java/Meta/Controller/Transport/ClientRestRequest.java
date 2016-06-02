package Meta.Controller.Transport;

import Meta.Controller.ClientRequest;
import Meta.Model.PatchFile;
import Protocols.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * @author Robin Duda
 */
class ClientRestRequest implements ClientRequest {
    private RoutingContext context;

    ClientRestRequest(RoutingContext context) {
        this.context = context;
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
                case ".json":
                    return "application/json";
                case ".svg":
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
        return context.request().getParam("file");
    }

    @Override
    public String version() {
        String version = context.request().getParam("version");

        if (version == null) {
            version = "9999.999.999";
        }

        return version;
    }
}
