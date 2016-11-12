package com.codingchili.services.Patching.Controller;

import com.codingchili.core.Exception.CoreException;
import com.codingchili.core.Exception.FileMissingException;
import com.codingchili.core.Protocol.*;

import com.codingchili.services.Patching.Configuration.PatchContext;
import com.codingchili.services.Patching.Model.PatchKeeper;
import com.codingchili.services.Patching.Model.PatchReloadedException;

import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.core.Protocol.Access.AUTHORIZED;
import static com.codingchili.core.Protocol.Access.PUBLIC;

/**
 * @author Robin Duda
 */
public class PatchHandler<T extends PatchContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<PatchRequest>> protocol = new Protocol<>();
    private final PatchKeeper patcher;

    public PatchHandler(T context) {
        super(context, NODE_PATCHING);

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
        } catch (PatchReloadedException e) {
            request.conflict(e);
        } catch (FileMissingException e) {
            request.error(e);
        }
    }
}
