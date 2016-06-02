package Meta;

import Configuration.Webserver.MetaServerSettings;
import Meta.Model.PatchInfo;
import Meta.Model.PatchKeeper;
import Meta.Model.PatchReloadedException;
import io.vertx.core.buffer.Buffer;

/**
 * @author Robin Duda
 */
public class ClientHandler {
    private MetaServerSettings settings;
    private PatchKeeper patcher;

    public ClientHandler(MetaProvider provider) {
        this.settings = provider.getSettings();
        this.patcher = provider.getPatchKeeper();

        provider.protocol()
                .use(ClientRequest.PATCH, this::patchinfo)
                .use(ClientRequest.GAMEINFO, this::gameinfo)
                .use(ClientRequest.NEWS, this::news)
                .use(ClientRequest.PATCHDATA, this::patchdata)
                .use(ClientRequest.FILE, this::file);
    }

    private void patchinfo(ClientRequest request) {
        PatchInfo patch = patcher.getPatchInfo();

        request.write(
                settings.getPatch()
                        .setName(patch.getName())
                        .setVersion(patch.getVersion())
        );
    }

    private void gameinfo(ClientRequest request) {
        request.write(settings.getGameinfo());
    }

    private void news(ClientRequest request) {
        request.write(settings.getNews());
    }

    private void patchdata(ClientRequest request) {
        request.write(patcher.getPatchInfo());
    }

    private void file(ClientRequest request) {
        try {
            request.file(Buffer.buffer(patcher.getPatchFile(request.file(), request.version()).getBytes()));
        } catch (PatchReloadedException e) {
            request.conflict();
        }
    }
}
