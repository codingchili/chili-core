package com.codingchili.core.listener;

import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.listener.transport.ClusterRequest;
import com.codingchili.core.protocol.ResponseStatus;
import static com.codingchili.core.configuration.CoreStrings.*;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class MultiHandlerTest {
    private static CoreContext context;
    private static final String ADDRESS = "address";
    private static final String ONE = "one";
    private static final String TWO = "two";

    @BeforeClass
    public static void setUp() {
        context = new SystemContext();
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void ensureHandlersCallable(TestContext test) {
        Async async = test.async(2);
        MultiHandler multi = new MultiHandler(
            new TestHandler(ONE),
            new TestHandler(TWO)
        ).setAddress(getClass().getSimpleName());

        context.handler(() -> multi).setHandler(done -> {
            test.assertTrue(done.succeeded());

            Stream.of(ONE, TWO).forEach(address -> {

                context.bus().send(getClass().getSimpleName(),
                    new JsonObject()
                        .put(PROTOCOL_ROUTE, ANY)
                        .put(PROTOCOL_TARGET, address),
                    (response) -> {
                        test.assertTrue(response.succeeded());
                        ClusterRequest request = new ClusterRequest(response.result());

                        test.assertEquals(request.data().getString(PROTOCOL_STATUS), ResponseStatus.ACCEPTED.name());
                        test.assertEquals(address, request.data().getString(ADDRESS));

                        async.countDown();
                    });
            });
        });
    }

    @Test
    public void ensureHandlerStoppedAfterStartup(TestContext test) {
        Async async = test.async();
        MultiHandler multi = new MultiHandler(
            new TestHandler(ONE)
        ).setAddress(getClass().getSimpleName());

        context.handler(() -> multi).setHandler(done -> {
            test.assertTrue(done.succeeded());

            multi.remove(ONE).setHandler(removed -> {
                context.bus().send(getClass().getSimpleName(),
                    new JsonObject()
                        .put(PROTOCOL_TARGET, ONE),
                    (response) -> {
                        test.assertTrue(response.succeeded());
                        ClusterRequest request = new ClusterRequest(response.result());

                        test.assertEquals(request.data().getString(PROTOCOL_STATUS), ResponseStatus.ERROR.name());
                        async.complete();
                    });
            });
        });
    }


    private class TestHandler implements CoreHandler {
        private String address;

        /**
         * @param address the address on which the handler is to be registered.
         */
        public TestHandler(String address) {
            this.address = address;
        }

        @Override
        public void handle(Request request) {
            request.write(new JsonObject().put(ADDRESS, address));
        }

        @Override
        public String address() {
            return address;
        }
    }
}
