package com.codingchili.core.protocol;

import com.codingchili.core.context.CoreRuntimeException;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.StartupListener;
import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

import static com.codingchili.core.configuration.CoreStrings.ANY;
import static com.codingchili.core.protocol.RoleMap.*;

/**
 * Maps packet data to authorizer and manages authentication status for authorizer.
 * <p>
 * Route documentation and listing may be retrieved using #{@link #getSchema()}
 * or by calling the protocol with the #{@link CoreStrings#PROTOCOL_DOCUMENTATION}
 * route. The documentation route is enabled whenever a handler class or route
 * is documented using either #{@link #document(String)}, #{@link #setDescription(String)}
 * or by adding the #{@link Description} annotation to the class or handler method.
 * <p>
 * Aug, 2020 - removed use of reflectasm as method reflection in J11 is 10% faster.
 * If setAccessible(true) is called then J11 reflection is 50% faster.
 */
public class Protocol<RequestType> {
    private AuthorizationHandler<RequestType> authorizer = new SimpleAuthorizationHandler<>();
    private String description = CoreStrings.getDescriptionMissing();
    private RoleType[] defaultRoles = new RoleType[]{RoleMap.get(USER)};
    private Function<Request, Future<RoleType>> authenticator = (Request) -> Future.succeededFuture(Role.PUBLIC);
    private Function<Request, String> routeMapper = Request::route;
    private boolean emitDocumentation = false;
    private Route<RequestType> lastAddedRoute;
    private AtomicBoolean dirty = new AtomicBoolean(true);
    private Class<?> dataModel;
    private Logger logger = new ConsoleLogger(getClass());
    private String target = "";

    {
        StartupListener.subscribe(core -> {
            logger = core.logger(getClass());
        });
    }

    public Protocol() {
    }

    /**
     * Creates a protocol by mapping annotated methods.
     *
     * @param handler contains methods to be mapped.
     */
    public Protocol(Receiver<RequestType> handler) {
        annotated(handler);
    }

    /**
     * Processes annotations on the given handler.
     *
     * @param handler the handler that is annotated
     * @return fluent
     */
    public Protocol<RequestType> annotated(Receiver<RequestType> handler) {
        setHandlerProperties(handler.getClass());
        setHandlerRoutes(handler);

        Address address = handler.getClass().getAnnotation(Address.class);

        if (address != null) {
            target = address.value();
        }
        dirty.set(true);
        return this;
    }

    /**
     * @param endpoint sets the endpoint of the api, this is only used for documentation purposes.
     * @return fluent
     */
    public Protocol<RequestType> endpoint(String endpoint) {
        this.target = endpoint;
        dirty.set(true);
        return this;
    }

    /**
     * Replaces the authorization handler used to map roles to routes.
     *
     * @param authorizer the new authorizer to use
     * @return fluent
     */
    public Protocol<RequestType> setAuthorizationHandler(AuthorizationHandler<RequestType> authorizer) {
        this.authorizer.list().forEach(authorizer::use);
        this.authorizer = authorizer;
        return this;
    }

    private void setHandlerProperties(Class<?> handlerClass) {
        Roles classRole = handlerClass.getAnnotation(Roles.class);
        Description classDescription = handlerClass.getAnnotation(Description.class);
        DataModel classModel = handlerClass.getAnnotation(DataModel.class);

        if (classDescription != null) {
            this.emitDocumentation = true;
            this.description = classDescription.value();
        }
        if (classRole != null) {
            this.defaultRoles = RoleMap.get(classRole.value());
        }
        if (classModel != null) {
            setDataModel(classModel.value());
        }
    }

    private void setHandlerRoutes(Receiver<RequestType> handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            if (!getClass().getModule().isNamed()) {
                handler.getClass().getModule().addOpens(
                        handler.getClass().getPackageName(),
                        getClass().getModule()
                );
            }
            // improve reflection performance by up to 50%.
            method.setAccessible(true);

            readMapper(method, handler);
            readAuthenticator(method, handler);
            readApi(method, handler);
        }
    }

    private void readMapper(Method method, Receiver<RequestType> handler) {
        RouteMapper mapper = method.getAnnotation(RouteMapper.class);

        if (mapper != null) {
            this.routeMapper = (request) -> {
                try {
                    return (String) method.invoke(handler, request);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    @SuppressWarnings("unchecked")
    private void readAuthenticator(Method method, Receiver<RequestType> handler) {
        Authenticator authenticator = method.getAnnotation(Authenticator.class);

        if (authenticator != null) {
            this.authenticator = (request) -> {
                try {
                    return (Future<RoleType>) method.invoke(handler, request);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    private void readApi(Method method, Receiver<RequestType> handler) {
        Api api = method.getAnnotation(Api.class);
        Description description = method.getAnnotation(Description.class);
        DataModel model = method.getAnnotation(DataModel.class);

        if (api != null) {
            String route = (api.route().isEmpty()) ? method.getName() : api.route();
            RoleType[] roles = (api.value()[0].equals(UNSET)) ? defaultRoles : RoleMap.get(api.value());

            wrap(route, handler, method, roles);

            if (description != null) {
                emitDocumentation = true;
                this.document(description.value());
            }

            if (model != null) {
                this.model(model.value());
            }
        }
    }

    /**
     * Sets the data model used for requests for documentation purposes.
     * Set automatically when using #{@link #annotated(Receiver)}
     *
     * @param model the data transfer object.
     * @return fluent
     */
    public Protocol<RequestType> setDataModel(Class<?> model) {
        this.dataModel = model;
        dirty.set(true);
        return this;
    }

    /**
     * Sets the default role of routes added _after_ this call.
     * <p>
     * Annotated alternative #{@link Roles} on handler class
     *
     * @param role the role to set for requests added after calling this method.
     * @return fluent
     */
    public Protocol<RequestType> setRole(RoleType... role) {
        this.defaultRoles = role;
        dirty.set(true);
        return this;
    }

    private void wrap(String route, Receiver<RequestType> handler, Method method, RoleType[] role) {
        use(route, request -> invokeMethod(method, handler, request), role);
    }

    @SuppressWarnings("unchecked")
    private <E> E invokeMethod(Method method, Object instance, Object argument) {
        try {
            if (method.getParameterCount() == 0) {
                return (E) method.invoke(instance);
            } else {
                return (E) method.invoke(instance, argument);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw throwAny(e.getCause()); // thrown inside throwAny method.
        }
    }

    public static RuntimeException throwAny(final Throwable throwable) {
        Protocol.<RuntimeException>throwAsUnchecked(throwable);
        throw new AssertionError("Internal error in exception rewrite.");
    }

    public static <T extends Exception> void throwAsUnchecked(Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Registers a handler for the given route.
     * <p>
     * Annotated alternative #{@link Api}
     *
     * @param route   the route to register a handler for.
     * @param handler the handler to be registered for the given route.
     * @return the updated protocol specification for fluent use.
     */
    public Protocol<RequestType> use(String route, RequestHandler<RequestType> handler) {
        use(route, handler, defaultRoles);
        return this;
    }

    /**
     * Registers a handler for the given route with an access level.
     * <p>
     * Annotated alternative #{@link Api}
     *
     * @param route   the route to register a handler for.
     * @param handler the handler to be registered for the given route with the access level.
     * @param role    specifies the authorization level required to access the route.
     * @return the updated protocol specification for fluent use.
     */
    public Protocol<RequestType> use(String route, RequestHandler<RequestType> handler, RoleType... role) {
        lastAddedRoute = new Route<>(route, handler, role);
        authorizer.use(lastAddedRoute);
        dirty.set(true);
        return this;
    }

    /**
     * Returns the route handler for the given target route and its access level.
     *
     * @param route the handler route to find.
     * @return the handler that is mapped to the route.
     * @throws AuthorizationRequiredException when authorization level is not fulfilled for the given route.
     * @throws HandlerMissingException        when the requested route handler is not registered.
     */
    public RequestHandler<RequestType> get(String route) throws AuthorizationRequiredException, HandlerMissingException {
        return get(route, Role.PUBLIC);
    }

    /**
     * Returns the route handler for the given target route and its access level.
     *
     * @param route the handler route to find
     * @param role  list of roles that are allowed to map to a route
     * @return the handler that is mapped to the route and access level.
     * @throws AuthorizationRequiredException when authorization level is not fulfilled for given route.
     * @throws HandlerMissingException        when the requested route handler is not registered.
     */
    public RequestHandler<RequestType> get(String route, RoleType role) throws AuthorizationRequiredException, HandlerMissingException {
        if (authorizer.contains(route)) {
            return authorizer.get(route, role);
        } else if (authorizer.contains(ANY)) {
            return authorizer.get(ANY, role);   // fallback to any route.
        } else {
            // no route registered, check if protocol emits documentation.
            if (emitDocumentation && route.equals(CoreStrings.PROTOCOL_DOCUMENTATION)) {
                return request -> {
                    if (request instanceof Request) {
                        ((Request) request).write(description());
                    }
                };
            } else {
                throw new HandlerMissingException(route);
            }
        }
    }

    // Cache the generated documentation and the serialization operation, as this could be
    // a potentially expensive operation for big api's.
    private Buffer descriptionCache;

    private Buffer description() {
        if (dirty.getAndSet(false)) {
            descriptionCache = Buffer.buffer(Serializer.yaml(getSchema()));
        }
        return descriptionCache;
    }

    /**
     * Processes a request with some additional error handling. The authenticator {@link #authenticator(Function)}
     * is invoked with a future to support asynchronous cryptography operations, the request size is checked against
     * the configured maximum value. The route to invoke may be configured by specifying a
     * custom {@link #routeMapper(Function)}. Any exceptions thrown by the invoked route, or if the route is missing
     * or authorization insufficient, an error will be written as a response to the request.
     *
     * @param request the request to be processed.
     */
    @SuppressWarnings("unchecked")
    public void process(Request request) {
        try {
            authenticator.apply(request).setHandler(done -> {
                if (done.succeeded()) {
                    try {
                        get(routeMapper.apply(request), done.result()).submit((RequestType) request);
                    } catch (Throwable e) {
                        logger.onError(e);
                        request.error(e);
                    }
                } else {
                    logger.onError(done.cause());
                    request.error(done.cause());
                }
            });
        } catch (Throwable e) {
            logger.onError(e);
            request.error(e);
        }
    }

    /**
     * Set the route mapper used to process requests. A route mapper determines which protocol route
     * that is to be invoked for the given request. The default mapper invokes {@link Request#route()}.
     *
     * @param mapper the mapper to use for this protocol instance.
     * @return fluent.
     */
    public Protocol<RequestType> routeMapper(Function<Request, String> mapper) {
        this.routeMapper = mapper;
        return this;
    }

    /**
     * Set the authenticator used to {@link #process(Request)} requests. The authenticator consumes
     * a Request and produces an optionally asynchronous result that indicates which role is valid
     * for the current request. The default authenticator responds with {@link Role#PUBLIC}.
     *
     * @param authenticator the authenticator to use for this protocol instance.
     * @return fluent.
     */
    public Protocol<RequestType> authenticator(Function<Request, Future<RoleType>> authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    /**
     * Adds a documentation string to the last added route.
     * <p>
     * Annotated alternative #{@link Description} on requesthandler class
     *
     * @param routeDescription route protocol description text
     * @return fluent
     */
    public Protocol<RequestType> document(String routeDescription) {
        Objects.requireNonNull(lastAddedRoute, CoreStrings.cannotDocumentBeforeUse());
        lastAddedRoute.setDescription(routeDescription);
        dirty.set(true);
        return this;
    }

    /**
     * Sets the model of the last added route.
     *
     * @param model the model to set for the route.
     * @return fluent
     */
    public Protocol<RequestType> model(Class<?> model) {
        Objects.requireNonNull(lastAddedRoute, CoreStrings.cannotSetModelBeforeUse());
        lastAddedRoute.setTemplate(model);
        dirty.set(true);
        return this;
    }

    /**
     * @return returns a list of all registered routes on the protoocol.
     */
    public ProtocolDescription<RequestType> getSchema() {
        return new ProtocolDescription<RequestType>()
                .setDescription(description)
                .setTemplate(dataModel)
                .setRoutes(authorizer.list())
                .setTarget(target);
    }

    /**
     * Sets the documentation string for this protocol suite.
     * <p>
     * Annotated alternative #{@link Description} on Handler class
     *
     * @param description description text.
     * @return fluent
     */
    public Protocol<RequestType> setDescription(String description) {
        this.description = description;
        this.emitDocumentation = true;
        dirty.set(true);
        return this;
    }

    /**
     * @return a list of all registered routes in the protocol.
     */
    public Set<String> available() {
        return authorizer.list().stream().map(Route::getRoute).collect(Collectors.toSet());
    }
}