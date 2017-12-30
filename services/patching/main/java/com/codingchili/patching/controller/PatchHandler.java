package com.codingchili.patching.controller;

import com.codingchili.patching.configuration.PatchContext;
import com.codingchili.patching.model.PatchKeeper;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * Handles patching requests.
 */
public class PatchHandler implements CoreHandler {
    private final Protocol<PatchRequest> protocol = new Protocol<>();
    private final PatchKeeper patcher;
    private PatchContext context;

    public PatchHandler(PatchContext context) {
        this.context = context;
        this.patcher = context.getPatchKeeper();

        protocol.setRole(Role.PUBLIC)
                .use(PATCH_IDENTIFIER, this::patchinfo)
                .use(PATCH_GAME_INFO, this::gameinfo)
                .use(PATCH_NEWS, this::news)
                .use(PATCH_DATA, this::patchdata)
                .use(ID_PING, Request::accept);
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route()).submit(new PatchRequest(request));
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
}
