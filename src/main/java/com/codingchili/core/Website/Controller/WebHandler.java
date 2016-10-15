package com.codingchili.core.Website.Controller;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Access;
import com.codingchili.core.Protocols.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocols.Exception.HandlerMissingException;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Website.Configuration.WebserverProvider;
import com.codingchili.core.Website.Model.CachedFileStore;

import java.nio.file.NoSuchFileException;

import static com.codingchili.core.Configuration.Strings.DIR_WEBSITE;
import static com.codingchili.core.Configuration.Strings.NODE_WEBSERVER;

/**
 * @author Robin Duda
 */
public class WebHandler extends AbstractHandler {
    private Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private CachedFileStore files;

    public WebHandler(WebserverProvider provider) {
        super(NODE_WEBSERVER);

        this.files = CachedFileStore.instance(provider.getVertx(), DIR_WEBSITE);

        protocol.use(Strings.ID_PING, Request::accept, Access.PUBLIC);
    }

    private void serve(Request request) {
        try {
            String file = request.action();

            if (request.action().equals("/")) {
                file = "/index.html";
            }

            request.write(files.getFile(file));
        } catch (NoSuchFileException e) {
            request.missing();
        }
    }

    @Override
    public void handle(Request request) throws AuthorizationRequiredException {
        try {
            protocol.get(Access.PUBLIC, request.action()).handle(request);
        } catch (HandlerMissingException e) {
            serve(request);
        }
    }
}
