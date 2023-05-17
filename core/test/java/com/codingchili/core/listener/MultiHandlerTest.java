package com.codingchili.core.listener;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.listener.transport.ClusterRequest;
import com.codingchili.core.protocol.ResponseStatus;

import static com.codingchili.core.configuration.CoreStrings.*;

@RunWith(VertxUnitRunner.class)
public class MultiHandlerTest {
    private static CoreContext context;
    private static final String ADDRESS = "address";
    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String THREE = "three";

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

        context.handler(() -> multi).onComplete(done -> {
            test.assertTrue(done.succeeded());

            Stream.of(ONE, TWO).forEach(address -> {

                context.bus().request(getClass().getSimpleName(),
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
                new TestHandler(THREE)
        ).setAddress(getClass().getSimpleName());

        context.handler(() -> multi).onComplete(done -> {
            test.assertTrue(done.succeeded());

            multi.remove(THREE).onComplete(removed -> {
                context.bus().request(getClass().getSimpleName(),
                        new JsonObject()
                                .put(PROTOCOL_TARGET, THREE),
                        (response) -> {
                            test.assertTrue(response.succeeded());
                            ClusterRequest request = new ClusterRequest(response.result());

                            System.out.println(request.data().encodePrettily());

                            test.assertEquals(ResponseStatus.ERROR.name(), request.data().getString(PROTOCOL_STATUS));
                            async.complete();
                        });
            });
        });
    }


    private class TestHandler implements CoreHandler<Request> {
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
