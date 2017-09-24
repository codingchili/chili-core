package com.codingchili.core.protocol;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;

/**
 * Protocol test cases using annotation mode.
 */
public class ProtocolAnnotationTest extends ProtocolTest {
    private AnnotatedRouter router = new AnnotatedRouter();

    @Override
    Protocol<Request> getProtocol() {
        return new Protocol<>(router);
    }

    @Override
    CoreHandler getHandler() {
        return router;
    }
}
