package com.codingchili.core.protocol;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;

// no @Address annotation and does not implement getAddress.
public class AnnotatedRouterNoAddress implements CoreHandler<Request> {
    @Override
    public void handle(Request request) {
        //
    }
}
