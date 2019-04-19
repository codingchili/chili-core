package com.codingchili.core.protocol;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.testing.EmptyRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codingchili.core.protocol.Role.*;

/**
 * Tests the protocol class and its authorization mechanism.
 */
@RunWith(VertxUnitRunner.class)
public abstract class ProtocolTest {
    static final String ADDRESS = "home";
    static final String DOC_PUBLIC = "a public route";
    static final String DOCSTRING_TEXT = "docstring text";
    static final String documentedRoute = "documentedRoute";
    static final String defaultRolePublic = "defaultRolePublic";
    static final String adminRoleRoute = "adminRoleRoute";
    static final String userRoleRoute = "userRoleRoute";
    static final String multipleUserRoute = "multipleUserRoute";
    static final String customRoleOnRoute = "customRoleOnRoute";
    static final String specialRoute = "specialRoute";
    static final String CUSTOM_ROLE = "CUSTOM_ROLE";
    private static final String MISSING = "missing";
    private static final int ROUTE_COUNT = 7;

    static {
        RoleMap.put(CUSTOM_ROLE, new RoleType() {
            @Override
            public String getName() {
                return "myCustomRole";
            }

            @Override
            public int getLevel() {
                return 999;
            }
        });
    }

    @Rule
    public Timeout timeout = new Timeout(2, TimeUnit.SECONDS);
    private Protocol<Request> protocol;
    private CoreHandler handler;

    @Before
    public void setUp() {
        protocol = getProtocol();
        handler = getHandler();
    }

    abstract Protocol<Request> getProtocol();

    abstract CoreHandler getHandler();

    @Test
    public void testHandlerMissing(TestContext test) throws Exception {
        Async async = test.async();

        try {
            protocol.get(MISSING);
            test.fail("Should throw handler missing exception.");
        } catch (HandlerMissingException e) {
            async.complete();
        }
    }

    @Test
    public void testPrivateRouteNoAccess(TestContext test) throws Exception {
        Async async = test.async();
        try {
            protocol.get(userRoleRoute, PUBLIC).submit(new EmptyRequest());
            test.fail("Should throw authorization exception.");
        } catch (AuthorizationRequiredException e) {
            async.complete();
        }
    }

    @Test
    public void testPublicRouteWithAccess(TestContext test) throws Exception {
        protocol.get(defaultRolePublic, USER).submit(onWrite(test.async(), defaultRolePublic));
    }

    @Test
    public void testUserRoute(TestContext test) throws Exception {
        protocol.get(userRoleRoute, USER).submit(onWrite(test.async(), userRoleRoute));
    }

    @Test
    public void testPublicRoute(TestContext test) throws Exception {
        protocol.get(defaultRolePublic).submit(onWrite(test.async(), defaultRolePublic));
    }

    /**
     * Completes the provided async if the route is matching what is written to the request.
     *
     * @param async the async to be completed
     * @param route the route that must match what is written to the request
     * @return a new request that handles the write event.
     */
    private EmptyRequest onWrite(Async async, String route) {
        return new EmptyRequest() {
            @Override
            public void write(Object o) {
                if (o.toString().equals(route)) {
                    async.complete();
                }
            }
        };
    }

    @Test
    public void testAdminAccessUserRole(TestContext test) {
        protocol.get(userRoleRoute, ADMIN).submit(onWrite(test.async(), userRoleRoute));
    }

    @Test
    public void testCustomRouteName(TestContext test) {
        protocol.get(specialRoute, PUBLIC).submit(onWrite(test.async(), specialRoute));
    }

    @Test
    public void testMultipleUsersHasAccess(TestContext test) {
        protocol.get(multipleUserRoute, USER).submit(onWrite(test.async(), multipleUserRoute));
        protocol.get(multipleUserRoute, ADMIN).submit(onWrite(test.async(), multipleUserRoute));
        try {
            protocol.get(multipleUserRoute, PUBLIC);
            test.fail("Role access should throw when multiple users and no permission.");
        } catch (AuthorizationRequiredException ignored) {
        }
    }

    @Test
    public void testCustomRole(TestContext test) {
        protocol.get(customRoleOnRoute, RoleMap.get(CUSTOM_ROLE))
                .submit(onWrite(test.async(), customRoleOnRoute));

        try {
            protocol.get(customRoleOnRoute, ADMIN)
                    .submit(onWrite(test.async(), customRoleOnRoute));
            test.fail("Admin role must not have access to custom role.");
        } catch (AuthorizationRequiredException ignored) {
        }
    }

    @Test
    public void testHandlerMissingAddress(TestContext test) {
        try {
            new AnnotatedRouterNoAddress().address();
            test.fail("Test case must fail when implementing class fails to provide address.");
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testHandlerAddress(TestContext test) {
        test.assertEquals(handler.address(), ADDRESS);
    }

    @Test
    public void testHandlerIsDocumented(TestContext test) {
        test.assertEquals(DOCSTRING_TEXT, protocol.getDescription().getDescription());
    }

    @Test
    public void testRouteIsDocumented(TestContext test) {
        AtomicBoolean hasAtleastOneDocumentedRoute = new AtomicBoolean(false);
        ProtocolDescription<?> list = protocol.getDescription();
        list.getRoutes().values().forEach(route -> {
            if (route.getDescription() != null) {
                hasAtleastOneDocumentedRoute.set(true);
            }
        });
        if (!hasAtleastOneDocumentedRoute.get()) {
            test.fail("No route descriptions found.");
        }
    }

    @Test
    public void testRouteDataModel(TestContext test) {
        protocol.getDescription().getRoutes().values().forEach(route -> {
            if (route.getRoute().equals(defaultRolePublic)) {
                test.assertNotEquals(0, route.getModel().values().size());
            }
        });
    }

    @Test
    public void testListRoutes(TestContext test) {
        ProtocolDescription<Request> mapping = protocol.getDescription();
        test.assertTrue(routeIsListed(documentedRoute));
        test.assertTrue(routeIsListed(defaultRolePublic));
        test.assertTrue(routeIsListed(adminRoleRoute));
        test.assertTrue(routeIsListed(userRoleRoute));
        test.assertEquals(ROUTE_COUNT, mapping.getRoutes().size());
    }

    private boolean routeIsListed(String route) {
        AtomicBoolean listed = new AtomicBoolean(false);
        protocol.getDescription().getRoutes().values().forEach(entry -> {
            if (entry.getRoute().equals(route))
                listed.set(true);
        });
        return listed.get();
    }
}
