package com.codingchili.social.controller;

import com.codingchili.common.*;
import com.codingchili.core.context.*;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Social handler to handle friendlists and xr-messaging.
 */
public class SocialHandler implements CoreHandler {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private ServiceContext context;

    @Override
    public void init(CoreContext context) {
        this.context = new SimpleServiceContext(context, SOCIAL_NODE);
        protocol.use(Strings.ID_PING, Request::accept, Access.PUBLIC);
    }

    @Override
    public void handle(Request request) throws CoreException {
        protocol.get(request.route()).handle(request);
    }

    @Override
    public ServiceContext context() {
        return context;
    }
}