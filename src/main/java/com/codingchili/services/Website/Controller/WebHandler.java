package com.codingchili.services.Website.Controller;

import com.codingchili.core.Configuration.CachedFileStoreSettings;
import com.codingchili.core.Exception.*;
import com.codingchili.core.Files.CachedFileStore;
import com.codingchili.core.Protocol.*;

import com.codingchili.services.Shared.Strings;
import com.codingchili.services.Website.Configuration.WebserverContext;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
public class WebHandler<T extends WebserverContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private final CachedFileStore files;

    public WebHandler(T context) {
        super(context, NODE_WEBSERVER);

        this.files = CachedFileStore.instance(context, new CachedFileStoreSettings()
                .setDirectory(DIR_WEBSITE)
                .setGzip(context.isGzip())
        );

        protocol.use(Strings.ID_PING, Request::accept, Access.PUBLIC);
    }

    private void serve(Request request) {
        try {
            String file = request.action();

            if (request.action().equals(DIR_ROOT)) {
                file = context.getStartPage();
                context.onPageLoaded(request);
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
            protocol.get(Access.PUBLIC, request.action()).handle(request);
        } catch (HandlerMissingException e) {
            serve(request);
        }
    }
}
