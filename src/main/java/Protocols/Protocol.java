package Protocols;

import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static Configuration.Strings.ERROR_HANDLER_MISSING_AUTHENTICATOR;

/**
 * @author Robin Duda
 */
public class Protocol {
    private AuthorizationHandler handlers = new AuthorizationHandler();
    private Method authenticator;

    public Protocol(Class clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Handler.class)) {

                Handler handler = method.getAnnotation(Handler.class);
                use(handler.value(), method, handler.access());
            }

            if (method.isAnnotationPresent(Authenticator.class)) {
                authenticator = method;
            }
        }

        if (authenticator == null) {
            throw new RuntimeException(ERROR_HANDLER_MISSING_AUTHENTICATOR);
        }
    }

    public void handle(HandlerProvider handler, Request request) throws AuthorizationRequiredException, HandlerMissingException {
        try {
            handlers.get(request.action(), access(handler, request)).invoke(handler, request);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Access access(HandlerProvider handler, Request request) {
        try {
            return (Access) authenticator.invoke(handler, request);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Access.PUBLIC;
    }

    private void use(String action, Method handler, Access access) {
        handlers.use(action, handler, access);
    }
}

