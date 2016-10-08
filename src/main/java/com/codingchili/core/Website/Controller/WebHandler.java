package com.codingchili.core.Website.Controller;

import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Website.Configuration.WebserverProvider;
import com.codingchili.core.Website.Model.CachedFileStore;

import java.nio.file.NoSuchFileException;

import static com.codingchili.core.Configuration.Strings.DIR_WEBSITE;
import static com.codingchili.core.Configuration.Strings.NODE_WEBSERVER;

/**
 * @author Robin Duda
 */
public class WebHandler extends AbstractHandler {
    private CachedFileStore files;

    public WebHandler(WebserverProvider provider) {
        super(NODE_WEBSERVER);

        this.files = CachedFileStore.instance(provider.getVertx(), DIR_WEBSITE);
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
    public void handle(Request request) {
        serve(request);
    }
}
