package Patching.Controller;

import Patching.Configuration.PatchProvider;
import Patching.Configuration.PatchServerSettings;
import Patching.Model.PatchKeeper;
import Patching.Model.PatchReloadedException;
import Protocols.*;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import Protocols.Util.Protocol;

import java.nio.file.NoSuchFileException;

import static Configuration.Strings.*;
import static Protocols.Access.AUTHORIZED;

/**
 * @author Robin Duda
 */
public class PatchHandler extends AbstractHandler {
    private Protocol<RequestHandler<PatchRequest>> protocol = new Protocol<>();
    private PatchServerSettings settings;
    private PatchKeeper patcher;

    public PatchHandler(PatchProvider provider) {
        super(NODE_PATCHING);

        this.settings = provider.getSettings();
        this.patcher = provider.getPatchKeeper();

        protocol.use(PATCH_IDENTIFIER, this::patchinfo)
                .use(PATCH_GAME_INFO, this::gameinfo)
                .use(PATCH_NEWS, this::news)
                .use(PATCH_DATA, this::patchdata)
                .use(PATCH_DOWNLOAD, this::download);

    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(AUTHORIZED, request.action()).handle((PatchRequest) request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
        }
    }

    private void patchinfo(PatchRequest request) {
        request.write(patcher.getPatchNotes());
    }

    private void gameinfo(PatchRequest request) {
        request.write(settings.getGameinfo());
    }

    private void news(PatchRequest request) {
        request.write(settings.getNews());
    }

    private void patchdata(PatchRequest request) {
        request.write(patcher.getDetails());
    }

    private void download(PatchRequest request) {
        try {
            request.file(patcher.getFile(request.file(), request.version()));
        } catch (PatchReloadedException e) {
            request.conflict();
        } catch (NoSuchFileException e) {
            request.missing();
        }
    }
}
