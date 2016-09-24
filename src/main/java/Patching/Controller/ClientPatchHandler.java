package Patching.Controller;

import Patching.Configuration.PatchProvider;
import Patching.Configuration.PatchServerSettings;
import Patching.Model.PatchKeeper;
import Patching.Model.PatchReloadedException;
import Protocols.*;

import java.nio.file.NoSuchFileException;

import static Configuration.Strings.*;
import static Protocols.Access.AUTHORIZE;

/**
 * @author Robin Duda
 */
public class ClientPatchHandler extends HandlerProvider {
    private PatchServerSettings settings;
    private PatchKeeper patcher;

    public ClientPatchHandler(PatchProvider provider) {
        super(ClientPatchHandler.class, provider.getLogger(), ADDRESS_PATCHING);

        this.settings = provider.getSettings();
        this.patcher = provider.getPatchKeeper();
    }

    @Authenticator
    public Access authenticate(Request request) {
        return AUTHORIZE;
    }

    @Handles(PATCH_IDENTIFIER)
    public void patchinfo(ClientRequest request) {
        request.write(patcher.getPatchNotes());
    }

    @Handles(PATCH_GAME_INFO)
    public void gameinfo(ClientRequest request) {
        request.write(settings.getGameinfo());
    }

    @Handles(PATCH_NEWS)
    public void news(ClientRequest request) {
        request.write(settings.getNews());
    }

    @Handles(PATCH_AUTHSERVER)
    public void authserver(ClientRequest request) {
        request.write(settings.getAuthserver());
    }

    @Handles(PATCH_DATA)
    public void patchdata(ClientRequest request) {
        request.write(patcher.getDetails());
    }

    @Handles(PATCH_DOWNLOAD)
    public void download(ClientRequest request) {
        try {
            request.file(patcher.getFile(request.file(), request.version()));
        } catch (PatchReloadedException e) {
            request.conflict();
        } catch (NoSuchFileException e) {
            request.missing();
        }
    }
}
