package Protocols;

import Authentication.Controller.ClientRequest;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Robin Duda
 */
public class Protocol {
    private AuthorizationHandler handlers = new AuthorizationHandler();

    public Protocol(Class clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Handler.class)) {

                Handler handler = m.getAnnotation(Handler.class);
                use(handler.value(), m, handler.access());
            }
        }
    }

    public void handle(HandlerProvider handler, Request request, Access access) throws AuthorizationRequiredException, HandlerMissingException {
        try {
            handlers.get(request.action(), access).invoke(handler, request);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void use(String action, Method handler, Access access) {
        handlers.use(action, handler, access);
    }
}

