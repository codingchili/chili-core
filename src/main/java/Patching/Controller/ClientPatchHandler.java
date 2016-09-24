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
import static Protocols.Access.AUTHORIZE;

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

    @Authenticator
    public Access authenticate(Request request) {
        return AUTHORIZE;
    }

    @Handler(value = PATCH_IDENTIFIER)
    private void patchinfo(ClientRequest request) {
        request.write(patcher.getPatchNotes());
    }

    @Handler(value = PATCH_GAME_INFO)
    private void gameinfo(ClientRequest request) {
        request.write(settings.getGameinfo());
    }

    @Handler(value = PATCH_NEWS)
    private void news(ClientRequest request) {
        request.write(settings.getNews());
    }

    @Handler(value = PATCH_AUTHSERVER)
    private void authserver(ClientRequest request) {
        request.write(settings.getAuthserver());
    }

    @Handler(value = PATCH_DATA)
    private void patchdata(ClientRequest request) {
        request.write(patcher.getDetails());
    }

    @Handler(value = PATCH_DOWNLOAD)
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
    public void process(Request request) throws AuthorizationRequiredException, HandlerMissingException {
        protocol.handle(this, request);
    }
}
