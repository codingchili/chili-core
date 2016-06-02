package Meta.Controller;

import Configuration.MetaServer.MetaServerSettings;
import Meta.Model.PatchInfo;
import Meta.Model.PatchKeeper;
import Meta.Model.PatchReloadedException;
import io.vertx.core.buffer.Buffer;

import java.nio.file.NoSuchFileException;

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
                .use(ClientRequest.AUTHSERVER, this::authserver)
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

    private void authserver(ClientRequest request) {
        request.write(settings.getAuthserver());
    }

    private void patchdata(ClientRequest request) {
        request.write(patcher.getPatchInfo());
    }

    private void file(ClientRequest request) {
        try {
            request.file(patcher.getPatchFile(request.file(), request.version()));
        } catch (PatchReloadedException e) {
            request.conflict();
        } catch (NoSuchFileException e) {
            request.missing();
        }
    }
}
