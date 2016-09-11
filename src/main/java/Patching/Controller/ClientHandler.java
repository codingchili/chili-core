package Patching.Controller;

import Configuration.Strings;
import Patching.Configuration.PatchProvider;
import Patching.Configuration.PatchServerSettings;
import Patching.Model.PatchKeeper;
import Patching.Model.PatchReloadedException;
import Protocols.PacketHandler;
import Protocols.Protocol;

import java.nio.file.NoSuchFileException;

/**
 * @author Robin Duda
 */
public class ClientHandler {
    private PatchServerSettings settings;
    private PatchKeeper patcher;

    public ClientHandler(PatchProvider provider) {
        this.settings = provider.getSettings();
        this.patcher = provider.getPatchKeeper();

        apply(provider.protocol());
    }

    public Protocol apply(Protocol<PacketHandler<ClientRequest>> protocol) {
        return protocol
                .use(Strings.PATCH_IDENTIFIER, this::patchinfo)
                .use(Strings.PATCH_GAME_INFO, this::gameinfo)
                .use(Strings.PATCH_NEWS, this::news)
                .use(Strings.PATCH_AUTHSERVER, this::authserver)
                .use(Strings.PATCH_DATA, this::patchdata)
                .use(Strings.PATCH_DOWNLOAD, this::download);
    }

    private void patchinfo(ClientRequest request) {
        request.write(patcher.getPatchNotes());
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
        request.write(patcher.getDetails());
    }

    private void download(ClientRequest request) {
        try {
            request.file(patcher.getFile(request.file(), request.version()));
        } catch (PatchReloadedException e) {
            request.conflict();
        } catch (NoSuchFileException e) {
            request.missing();
        }
    }
}
