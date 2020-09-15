package com.codingchili.core.protocol;

import com.codingchili.core.context.CoreException;
import com.codingchili.core.context.CoreRuntimeException;
import io.vertx.core.Future;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.testing.StorageObject;

import static com.codingchili.core.protocol.ProtocolTest.*;
import static com.codingchili.core.protocol.RoleMap.*;

@Roles(PUBLIC)
@Description(ProtocolTest.DOCSTRING_TEXT)
@Address(ADDRESS)
public class AnnotatedRouter implements CoreHandler {

    @Api
    @Description(DOC_PUBLIC)
    public void documentedRoute(Request request) {
        request.write(documentedRoute);
    }

    @Api
    @DataModel(StorageObject.class)
    public void defaultRolePublic(Request request) {
        request.write(defaultRolePublic);
    }

    @Api(ADMIN)
    public void adminRoleRoute(Request request) {
        request.write(adminRoleRoute);
    }

    @Api(USER)
    public void userRoleRoute(Request request) {
        request.write(userRoleRoute);
    }

    @Api(route = specialRoute)
    public void customRouteName(Request request) {
        request.write(specialRoute);
    }

    @Api({USER, ADMIN})
    public void multipleUserRoute(Request request) {
        request.write(multipleUserRoute);
    }

    @Api(CUSTOM_ROLE)
    public void customRoleOnRoute(Request request) {
        request.write(customRoleOnRoute);
    }

    @Api(PUBLIC)
    public void throwMappedException(Request request) {
        throw new CoreRuntimeException("Core runtime exception retains message");
    }

    @Override
    public void handle(Request request) {
        // unused: protocol is called directly by tests.
    }

    @Authenticator
    public Future<Role> authenticate(Request request) {
        return Future.succeededFuture(Role.PUBLIC);
    }

    @RouteMapper
    public String map(Request request) {
        return request.route();
    }
}
