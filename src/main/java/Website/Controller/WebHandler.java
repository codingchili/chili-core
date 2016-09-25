package Website.Controller;

import Configuration.Strings;
import Protocols.*;
import Website.Configuration.WebserverProvider;
import Website.Model.CachedFileStore;

import java.nio.file.NoSuchFileException;

import static Configuration.Strings.ANY;
import static Configuration.Strings.DIR_WEBSITE;
import static Protocols.Access.AUTHORIZED;

/**
 * @author Robin Duda
 */
public class WebHandler extends HandlerProvider {
    private CachedFileStore files;

    public WebHandler(WebserverProvider provider) {
        super(WebHandler.class, provider.getLogger(), Strings.NODE_WEBSERVER);

        this.files = CachedFileStore.instance(provider.getVertx(), DIR_WEBSITE);
    }

    @Authenticator
    public Access authenticator(Request request) {
        return AUTHORIZED;
    }

    @Handles(ANY)
    public void serve(Request request) {
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
}
