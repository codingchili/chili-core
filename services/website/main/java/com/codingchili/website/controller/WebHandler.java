package com.codingchili.website.controller;

import com.codingchili.common.*;
import com.codingchili.core.configuration.*;
import com.codingchili.core.context.*;
import com.codingchili.core.files.*;
import com.codingchili.core.files.exception.*;
import com.codingchili.core.protocol.*;
import com.codingchili.core.protocol.exception.*;
import com.codingchili.website.configuration.*;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Handles incoming requests for files. (website files)
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
            String file = request.route();

            if (file.equals(EMPTY)) {
                file = context.getStartPage();
                context.onPageLoaded(request);
            }

            request.write(files.getFile(file));
        } catch (FileMissingException e) {
            request.error(e);
        }
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(Access.PUBLIC, request.route()).handle(request);
    }
}
