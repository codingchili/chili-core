package Patching.Controller;

import Patching.Configuration.PatchProvider;
import Patching.Configuration.PatchServerSettings;
import Patching.Model.PatchKeeper;
import Patching.Model.PatchReloadedException;
import Protocols.*;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

import java.nio.file.NoSuchFileException;

import static Configuration.Strings.*;
import static Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 */
public class ClientPatchHandler implements HandlerProvider {
    private Protocol protocol = new Protocol(this.getClass());
    private PatchServerSettings settings;
    private PatchKeeper patcher;

    public ClientPatchHandler(PatchProvider provider) {
        this.settings = provider.getSettings();
        this.patcher = provider.getPatchKeeper();
    }

    @Handler(value = PATCH_IDENTIFIER, access = PUBLIC)
    private void patchinfo(ClientRequest request) {
        request.write(patcher.getPatchNotes());
    }

    @Handler(value = PATCH_GAME_INFO, access = PUBLIC)
    private void gameinfo(ClientRequest request) {
        request.write(settings.getGameinfo());
    }

    @Handler(value = PATCH_NEWS, access = PUBLIC)
    private void news(ClientRequest request) {
        request.write(settings.getNews());
    }

    @Handler(value = PATCH_AUTHSERVER, access = PUBLIC)
    private void authserver(ClientRequest request) {
        request.write(settings.getAuthserver());
    }

    @Handler(value = PATCH_DATA, access = PUBLIC)
    private void patchdata(ClientRequest request) {
        request.write(patcher.getDetails());
    }

    @Handler(value = PATCH_DOWNLOAD, access = PUBLIC)
    private void download(ClientRequest request) {
        try {
            request.file(patcher.getFile(request.file(), request.version()));
        } catch (PatchReloadedException e) {
            request.conflict();
        } catch (NoSuchFileException e) {
            request.missing();
        }
    }

    @Override
    public void process(Request request, Access access) throws AuthorizationRequiredException, HandlerMissingException {
        protocol.handle(this, request, access);
    }
}
