package Patching.Controller;

import Patching.Configuration.PatchProvider;
import Patching.Configuration.PatchServerSettings;
import Patching.Model.PatchKeeper;
import Patching.Model.PatchReloadedException;
import Protocols.*;

import java.nio.file.NoSuchFileException;

import static Configuration.Strings.*;
import static Protocols.Access.AUTHORIZED;

/**
 * @author Robin Duda
 */
public class PatchHandler extends HandlerProvider {
    private PatchServerSettings settings;
    private PatchKeeper patcher;

    public PatchHandler(PatchProvider provider) {
        super(PatchHandler.class, provider.getLogger(), NODE_PATCHING);

        this.settings = provider.getSettings();
        this.patcher = provider.getPatchKeeper();
    }

    @Authenticator
    public Access authenticate(Request request) {
        return AUTHORIZED;
    }

    @Handles(PATCH_IDENTIFIER)
    public void patchinfo(PatchRequest request) {
        request.write(patcher.getPatchNotes());
    }

    @Handles(PATCH_GAME_INFO)
    public void gameinfo(PatchRequest request) {
        request.write(settings.getGameinfo());
    }

    @Handles(PATCH_NEWS)
    public void news(PatchRequest request) {
        request.write(settings.getNews());
    }

    @Handles(PATCH_DATA)
    public void patchdata(PatchRequest request) {
        request.write(patcher.getDetails());
    }

    @Handles(PATCH_DOWNLOAD)
    public void download(PatchRequest request) {
        try {
            request.file(patcher.getFile(request.file(), request.version()));
        } catch (PatchReloadedException e) {
            request.conflict();
        } catch (NoSuchFileException e) {
            request.missing();
        }
    }
}
