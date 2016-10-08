package com.codingchili.core.Patching;

import com.codingchili.core.Logging.LoggerMock;
import com.codingchili.core.Logging.Model.Logger;
import com.codingchili.core.Patching.Configuration.PatchProvider;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
class ProviderMock extends PatchProvider {


    public ProviderMock(Vertx vertx) {
        super(vertx);
    }


    @Override
    public Logger getLogger() {
        return new LoggerMock();
    }
}
