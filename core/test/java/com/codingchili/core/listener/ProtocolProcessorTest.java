package com.codingchili.core.listener;

import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Role;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import com.codingchili.core.testing.ContextMock;
import com.codingchili.core.testing.RequestMock;
import com.codingchili.core.testing.ResponseListener;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;
import static com.codingchili.core.protocol.ResponseStatus.*;

/**
 * Tests for {@link Protocol#process(Request)}
 * <p>
 * Previously RequestProcessor.
 */
@RunWith(VertxUnitRunner.class)
public class ProtocolProcessorTest {
    private static final String TEST_HANDLER = "test-handler";
    private ContextMock mock;

    @Before
    public void setUp() {
        mock = new ContextMock();
    }

    @After
    public void tearDown(TestContext test) {
        mock.close(test.asyncAssertSuccess());
    }

    @Test
    public void requestSizeTooLarge(TestContext test) {
        Request request = new Request() {
            @Override
            public void write(Object object) {
                test.assertEquals(BAD.name(), ((JsonObject) object).getString(PROTOCOL_STATUS));
            }

            @Override
            public Connection connection() {
                return new Connection(this::write, "test_id");
            }

            @Override
            public JsonObject data() {
                return new JsonObject();
            }

            @Override
            public int size() {
                return Integer.MAX_VALUE;
            }
        };
        processor((toHandler) -> test.fail("handler should not be called if pre-validation fails."))
                .accept(() -> request);
    }

    @Test
    public void requestHandlerNotFound(TestContext test) {
        processor((request) -> {
            throw new HandlerMissingException(TEST_HANDLER);
        }).accept(() -> request((response, status) -> {
            test.assertEquals(ERROR, status);
            test.assertTrue(response.encode().contains(TEST_HANDLER));
        }));
    }

    @Test
    public void handlerThrowsAnException(TestContext test) {
        processor((request) -> {
            throw new RuntimeException(TEST_HANDLER);
        }).accept(() -> request((response, status) -> {
            test.assertEquals(ERROR, status);
        }));
    }

    @Test
    public void handlerCompletesWithSuccess(TestContext test) {
        processor(Request::accept)
                .accept(() -> request((response, status) ->
                        test.assertEquals(ACCEPTED, status)));
    }

    @Test
    public void errorHandledInSupplier(TestContext test) {
        processor(Request::accept)
                .accept(() -> {
                    // this error is not thrown, its only logged.
                    // since the supplier fails, there is no request to write the error to.
                    throw new RuntimeException("handle me.");
                });
    }

    private Request request(ResponseListener listener) {
        return RequestMock.get("test", listener);
    }

    private Consumer<Supplier<Request>> processor(Consumer<Request> consumer) {
        return (runnable) -> {
            new TestHandler(consumer).handle(runnable.get());
        };
    }

    private class TestHandler implements CoreHandler {
        private Protocol<Request> protocol = new Protocol<Request>().setRole(Role.PUBLIC);
        private Consumer<Request> handler;

        public TestHandler(Consumer<Request> handler) {
            this.handler = handler;
            protocol.use("test", this::test);
        }

        private void test(Request request) {
            handler.accept(request);
        }

        @Override
        public void handle(Request request) {
            protocol.process(request);
        }

    }
}