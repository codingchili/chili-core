package com.codingchili.core.Patching.Controller;

import com.codingchili.core.Patching.Configuration.PatchProvider;
import com.codingchili.core.Patching.Configuration.PatchServerSettings;
import com.codingchili.core.Patching.Model.PatchKeeper;
import com.codingchili.core.Patching.Model.PatchReloadedException;
import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;

import java.nio.file.NoSuchFileException;

import static com.codingchili.core.Configuration.Strings.*;
import static com.codingchili.core.Protocols.Access.AUTHORIZED;
import static com.codingchili.core.Protocols.Access.PUBLIC;

/**
 * @author Robin Duda
 */
public class PatchHandler extends AbstractHandler {
    private final Protocol<RequestHandler<PatchRequest>> protocol = new Protocol<>();
    private final PatchServerSettings settings;
    private final PatchKeeper patcher;

    public PatchHandler(PatchProvider provider) {
        super(NODE_PATCHING);

        this.settings = provider.getSettings();
        this.patcher = provider.getPatchKeeper();

        protocol.use(PATCH_IDENTIFIER, this::patchinfo)
                .use(PATCH_GAME_INFO, this::gameinfo)
                .use(PATCH_NEWS, this::news)
                .use(PATCH_DATA, this::patchdata)
                .use(PATCH_DOWNLOAD, this::download)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) throws ProtocolException {
        protocol.get(AUTHORIZED, request.action()).handle(new PatchRequest(request));
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
