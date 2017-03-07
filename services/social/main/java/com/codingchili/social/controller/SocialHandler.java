package com.codingchili.social.controller;

import com.codingchili.common.Strings;
import com.codingchili.social.configuration.SocialContext;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.protocol.*;

/**
 * @author Robin Duda
 */
public class SocialHandler<T extends SocialContext> extends AbstractHandler<T> {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();

    public SocialHandler(T context) {
        super(context, Strings.SOCIAL_NODE);

        protocol.use(Strings.ID_PING, Request::accept, Access.PUBLIC)
                .use(Strings.ID_LICENSE, this::messageHandler, Access.PUBLIC);
    }

    private void messageHandler(Request request) {
        request.write(context.message());
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(request.route()).handle(request);
    }
}
