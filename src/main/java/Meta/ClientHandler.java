package Meta;

import Configuration.Webserver.MetaServerSettings;

/**
 * @author Robin Duda
 */
public class ClientHandler {
    private MetaServerSettings settings;

    public ClientHandler(MetaProvider provider) {
        this.settings = provider.getSettings();

        // todo read all files in /resources and generate patch data
        // todo file watcher to reload on change

        provider.protocol()
                .use(ClientRequest.PATCH, this::patchinfo)
                .use(ClientRequest.GAMEINFO, this::gameinfo)
                .use(ClientRequest.NEWS, this::news)
                .use(ClientRequest.FILE, this::file);
    }

    private void patchinfo(ClientRequest request) {
        request.write(settings.getPatch());
    }

    private void gameinfo(ClientRequest request) {
        request.write(settings.getGameinfo());
    }

    private void news(ClientRequest request) {
        request.write(settings.getGameinfo());
    }

    private void file(ClientRequest request) {

    }
}
