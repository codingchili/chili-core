package Website.Controller;

import Protocols.AbstractHandler;
import Protocols.Request;
import Website.Configuration.WebserverProvider;
import Website.Model.CachedFileStore;

import java.nio.file.NoSuchFileException;

import static Configuration.Strings.DIR_WEBSITE;
import static Configuration.Strings.NODE_WEBSERVER;

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
