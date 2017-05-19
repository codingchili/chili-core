package com.codingchili.social.controller;

import com.codingchili.common.Strings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.context.SimpleServiceContext;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Access;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RequestHandler;

import static com.codingchili.common.Strings.SOCIAL_NODE;

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
    public void handle(Request request) {
        protocol.get(request.route()).handle(request);
    }

    @Override
    public String address() {
        return SOCIAL_NODE;
    }
}