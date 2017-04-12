package com.codingchili.patching.controller;

import com.codingchili.core.context.*;
import com.codingchili.core.files.exception.*;
import com.codingchili.core.protocol.*;
import com.codingchili.patching.configuration.*;
import com.codingchili.patching.model.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.protocol.Access.*;

/**
 * @author Robin Duda
 *         <p>
 *         Handles patching requests.
 */
public class PatchHandler implements CoreHandler {
    private final Protocol<RequestHandler<PatchRequest>> protocol = new Protocol<>();
    private final PatchKeeper patcher;
    private PatchContext context;

    public PatchHandler(PatchContext context) {
        this.context = context;
        this.patcher = context.getPatchKeeper();

        protocol.use(PATCH_IDENTIFIER, this::patchinfo)
                .use(PATCH_GAME_INFO, this::gameinfo)
                .use(PATCH_NEWS, this::news)
                .use(PATCH_DATA, this::patchdata)
                .use(PATCH_DOWNLOAD, this::download)
                .use(PATCH_WEBSEED, this::webseed)
                .use(ID_PING, Request::accept, PUBLIC);
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(AUTHORIZED, request.route()).handle(new PatchRequest(request));
    }

    @Override
    public ServiceContext context() {
        return context;
    }

    @Override
    public String address() {
        return NODE_PATCHING;
    }

    private void patchinfo(PatchRequest request) {
        request.write(context.notes());
    }

    private void gameinfo(PatchRequest request) {
        request.write(context.gameinfo());
    }

    private void news(PatchRequest request) {
        request.write(context.news());
    }

    private void patchdata(PatchRequest request) {
        request.write(patcher.getDetails());
    }

    private void webseed(PatchRequest request) {
        try {
            request.write(patcher.getBuffer(request.webseedFile(), request.start(), request.end()));
        } catch (FileMissingException e) {
            request.error(e);
        }
    }

    private void download(PatchRequest request) {
        try {
            request.file(patcher.getFile(request.file(), request.version()));
        } catch (PatchReloadedException | FileMissingException e) {
            request.error(e);
        }
    }
}
