package com.codingchili.website.controller;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.files.CachedFileStore;
import com.codingchili.core.files.exception.FileMissingException;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;
import com.codingchili.website.configuration.WebserverContext;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * Handles incoming requests for files. (website files)
 */
public class WebHandler implements CoreHandler<Request> {
    private final Protocol<Request> protocol = new Protocol<>();
    private final CachedFileStore files;
    private WebserverContext context;

    public WebHandler(WebserverContext context) {
        this.context = context;

        this.files = new CachedFileStore(context, new CachedFileStoreSettings()
                .setDirectory(context.resources())
                .setGzip(context.isGzip()));

        protocol.use(Strings.ID_PING, Request::accept, Role.PUBLIC)
                .use(ANY, this::serve, Role.PUBLIC);
    }

    private void serve(Request request) {
        try {
            String file = request.route();

            if (file.equals(EMPTY)) {
                file = context.getStartPage();
                context.onPageLoaded(request);
            }

            request.write(files.getFile(file).getBuffer());
        } catch (FileMissingException e) {
            request.error(e);
        }
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route(), Role.PUBLIC).submit(request);
    }

    @Override
    public String address() {
        return NODE_WEBSERVER;
    }
}
