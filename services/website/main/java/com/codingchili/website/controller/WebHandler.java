package com.codingchili.website.controller;

import com.codingchili.common.Strings;
import com.codingchili.website.configuration.WebserverContext;

import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.configuration.exception.FileMissingException;
import com.codingchili.core.files.CachedFileStore;
import com.codingchili.core.protocol.*;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *
 * Handles incoming requests for files. (website files)
 */
public class WebHandler<T extends WebserverContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private final CachedFileStore files;

    public WebHandler(T context) {
        super(context, NODE_WEBSERVER);

        this.files = new CachedFileStore(context, new CachedFileStoreSettings()
                .setDirectory(context.resources())
                .setGzip(context.isGzip()));

        files.initialize();

        protocol.use(Strings.ID_PING, Request::accept, Access.PUBLIC)
                .use(ANY, this::serve, Access.PUBLIC);
    }

    private void serve(Request request) {
        try {
            String file;

            if (request.route().equals(DIR_ROOT)) {
                file = context.getStartPage();
                context.onPageLoaded(request);
            } else {
                file = request.route().replaceFirst("^/", "");
            }

            request.write(files.getFile(file));
        } catch (FileMissingException e) {
            try {
                request.write(files.getFile(context.getMissingPage()));
            } catch (FileMissingException e1) {
                request.error(e1);
            }
        }
    }

    @Override
    public void handle(Request request) throws AuthorizationRequiredException {
        try {
            protocol.get(Access.PUBLIC, request.route()).handle(request);
        } catch (HandlerMissingException e) {
            request.error(e);
        }
    }
}
