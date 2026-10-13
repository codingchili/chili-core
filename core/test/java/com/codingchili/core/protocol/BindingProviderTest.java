package com.codingchili.core.protocol;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;
import com.codingchili.core.testing.RequestMock;

import static com.codingchili.core.protocol.ResponseStatus.*;

/**
 * Tests that the strategy used to bind handler methods is swappable, and that the strategies behave the same.
 */
@RunWith(VertxUnitRunner.class)
public class BindingProviderTest {
    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

    record Echo(String text) {
    }

    @Roles(RoleMap.PUBLIC)
    public static class SampleHandler implements Receiver<Request> {
        final AtomicInteger pings = new AtomicInteger();
        private final Protocol<Request> protocol;

        SampleHandler(BindingProvider provider) {
            protocol = new Protocol<>(this, provider);
        }

        @Api
        public void ping() {
            pings.incrementAndGet();
        }

        @Api
        public void legacy(Request request) {
            request.write(new JsonObject().put("legacy", true));
        }

        @Api
        public Echo echo(Echo input) {
            return input;
        }

        @Api
        public void fail(Request request) {
            throw new IllegalStateException("boom");
        }

        @Override
        public void handle(Request request) {
            protocol.process(request);
        }
    }

    @Test
    public void testReflectionBindings(TestContext test) {
        verify(test, BindingProvider.REFLECTION);
    }

    @Test
    public void testMethodHandleBindings(TestContext test) {
        verify(test, BindingProvider.METHOD_HANDLES);
    }

    @Test
    public void testCustomProviderIsUsed(TestContext test) {
        AtomicInteger bound = new AtomicInteger();
        SampleHandler handler = new SampleHandler(method -> {
            bound.incrementAndGet();
            return BindingProvider.REFLECTION.bind(method);
        });
        // one binding for each annotated route.
        test.assertEquals(4, bound.get());
        handler.handle(RequestMock.get("ping", (json, status) -> {}, new JsonObject()));
        test.assertEquals(1, handler.pings.get());
    }

    private void verify(TestContext test, BindingProvider provider) {
        SampleHandler handler = new SampleHandler(provider);

        // a route without parameters does not answer, it is observed by its side effect.
        handler.handle(RequestMock.get("ping", (json, status) -> {}, new JsonObject()));
        test.assertEquals(1, handler.pings.get());
        call(test, handler, "legacy", new JsonObject(), (json, status) ->
                test.assertTrue(json.getBoolean("legacy")));
        call(test, handler, "echo", new JsonObject().put("text", "hi"), (json, status) ->
                test.assertEquals("hi", json.getString("text")));
        // the exception of the method is answered as is, not wrapped in the exception of the binding.
        call(test, handler, "fail", new JsonObject(), (json, status) -> {
            test.assertEquals(ERROR, status);
        });
    }

    private void call(TestContext test, SampleHandler handler, String route, JsonObject data, com.codingchili.core.testing.ResponseListener listener) {
        Async async = test.async();

        handler.handle(RequestMock.get(route, (json, status) -> {
            listener.handle(json, status);
            async.complete();
        }, data));
    }
}
