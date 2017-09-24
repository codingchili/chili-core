package com.codingchili.core.protocol;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;

public class ProtocolBuilderTest extends ProtocolTest {
    private SimpleBuilderRouter handler = new SimpleBuilderRouter();

    @Override
    Protocol<Request> getProtocol() {
        return handler.getProtocol();
    }

    @Override
    CoreHandler getHandler() {
        return handler;
    }
}
