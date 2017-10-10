package com.codingchili.core.listener;

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

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;
import static com.codingchili.core.protocol.ResponseStatus.BAD;
import static com.codingchili.core.protocol.ResponseStatus.ERROR;

@RunWith(VertxUnitRunner.class)
public class RequestProcessorTest {
    public static final String TEST_HANDLER = "test-handler";
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
            public JsonObject data() {
                return null;
            }

            @Override
            public int size() {
                return Integer.MAX_VALUE;
            }
        };
       processor((toHandler) -> test.fail("handler should not be called if pre-validation fails."))
               .submit(request);
    }

    @Test
    public void requestHandlerNotFound(TestContext test) {
        processor((request) -> {
            throw new HandlerMissingException(TEST_HANDLER);
        }).submit(request((response, status) -> {
            test.assertEquals(ERROR, status);
            test.assertTrue(response.encode().contains(TEST_HANDLER));
        }));
    }

    @Test
    public void handlerThrowsAnException(TestContext test) {
        processor((request) -> {
            throw new RuntimeException(TEST_HANDLER);
        }).submit(request((response, status) -> {
            test.assertEquals(ERROR, status);
        }));
    }

    @Test
    public void handlerCompletesWithSuccess(TestContext test) {
        processor(Request::accept)
                .submit(request((response, status) ->
                        test.assertEquals(ACCEPTED, status)));
    }

    private Request request(ResponseListener listener) {
        return RequestMock.get("", listener);
    }

    private RequestProcessor processor(Consumer<Request> request) {
        return new RequestProcessor(new ContextMock(), new TestHandler(request));
    }

    private class TestHandler implements CoreHandler {
        private Consumer<Request> handler;

        public TestHandler(Consumer<Request> handler) {
            this.handler = handler;
        }

        @Override
        public void handle(Request request) {
            handler.accept(request);
        }

    }
}