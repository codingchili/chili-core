package com.codingchili.core.status;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientAgent;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.testing.ContextMock;

/**
 * Tests the readiness endpoint, the status report and the status page.
 */
@RunWith(VertxUnitRunner.class)
public class StatusServiceTest {
    private static final String LOOPBACK = "127.0.0.1";
    private static final Pattern NONCE = Pattern.compile("script-src 'nonce-([A-Za-z0-9_-]+)'");
    private ShuttingDownContext context;
    private StatusService service;
    private ListenerSettings settings;
    // a client that is garbage collected closes its connections: it is kept for the whole test.
    private HttpClientAgent client;

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.SECONDS);

    @Before
    public void setUp(TestContext test) {
        context = new ShuttingDownContext();
        service = new StatusService().bind(LOOPBACK);
        settings = new ListenerSettings().setPort(0).setSecure(false);
        service.settings(settings);
        client = context.vertx().createHttpClient();
    }

    @After
    public void tearDown(TestContext test) {
        context.close().onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testLivenessIsUp(TestContext test) {
        deployed(test, () -> get("/health/live")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(200, response.status);
            test.assertEquals("UP", response.json().getString("status"));
        }));
    }

    @Test
    public void testReadyWhenAllChecksPass(TestContext test) {
        service.check("storage", () -> Future.succeededFuture());

        deployed(test, () -> get("/health/ready")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(200, response.status);
            JsonObject json = response.json();
            test.assertTrue(json.getBoolean("ready"));
            test.assertEquals("UP", json.getJsonObject("checks").getJsonObject("storage").getString("status"));
            test.assertEquals("UP", json.getJsonObject("checks").getJsonObject("shutdown").getString("status"));
        }));
    }

    @Test
    public void testNotReadyWhenACheckFails(TestContext test) {
        service.check("storage", () -> Future.failedFuture("storage unreachable"));
        service.check("other", () -> Future.succeededFuture());

        deployed(test, () -> get("/health/ready")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(503, response.status);
            JsonObject json = response.json();
            test.assertFalse(json.getBoolean("ready"));
            JsonObject checks = json.getJsonObject("checks");
            test.assertEquals("DOWN", checks.getJsonObject("storage").getString("status"));
            test.assertEquals("storage unreachable", checks.getJsonObject("storage").getString("message"));
            // the check that passes is still reported as passing.
            test.assertEquals("UP", checks.getJsonObject("other").getString("status"));
        }));
    }

    @Test
    public void testNotReadyWhenACheckThrows(TestContext test) {
        service.check("broken", () -> {
            throw new IllegalStateException("check is broken");
        });

        deployed(test, () -> get("/health/ready")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(503, response.status);
            test.assertEquals("check is broken", response.json().getJsonObject("checks")
                    .getJsonObject("broken").getString("message"));
        }));
    }

    @Test
    public void testNotReadyWhenACheckTimesOut(TestContext test) {
        service.checkTimeout(100);
        // never completes.
        service.check("hanging", () -> Promise.<Void>promise().future());

        deployed(test, () -> get("/health/ready")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(503, response.status);
            test.assertEquals("timed out after 100 ms", response.json().getJsonObject("checks")
                    .getJsonObject("hanging").getString("message"));
        }));
    }

    @Test
    public void testNotReadyWhileShuttingDownButStillAlive(TestContext test) {
        deployed(test, () -> {
            context.down = true;
            return get("/health/ready").compose(ready -> get("/health/live").map(live -> new Response[]{ready, live}));
        }).onComplete(test.asyncAssertSuccess(responses -> {
            Response ready = responses[0];
            test.assertEquals(503, ready.status);
            test.assertEquals("shutting down", ready.json().getJsonObject("checks")
                    .getJsonObject("shutdown").getString("message"));
            // a shutdown is not a reason to kill the process.
            test.assertEquals(200, responses[1].status);
        }));
    }

    @Test
    public void testHeadHasTheStatusOfGetAndNoBody(TestContext test) {
        service.check("storage", () -> Future.failedFuture("down"));

        deployed(test, () -> request(HttpMethod.HEAD, "/health/ready")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(503, response.status);
            test.assertEquals(0, response.body.length());
        }));
    }

    @Test
    public void testStatusReportContainsAllSections(TestContext test) {
        deployed(test, () -> get("/status")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(200, response.status);
            JsonObject report = response.json();

            test.assertEquals("RUNNING", report.getString("state"));
            test.assertTrue(report.getBoolean("ready"));
            test.assertNotNull(report.getString("timestamp"));
            test.assertTrue(report.getJsonObject("application").getLong("uptimeMs") > 0);
            test.assertTrue(report.getJsonObject("application").getLong("pid") > 0);
            test.assertTrue(report.getJsonObject("jvm").getJsonObject("heap").getDouble("usedMb") > 0);
            test.assertTrue(report.getJsonObject("jvm").getJsonObject("threads").getInteger("live") > 0);
            test.assertFalse(report.getJsonObject("jvm").getJsonArray("gc").isEmpty());
            test.assertFalse(report.getJsonObject("vertx").getBoolean("clustered"));
            test.assertTrue(report.getJsonObject("vertx").getInteger("workerPoolSize") > 0);
            test.assertTrue(report.getJsonObject("vertx").getInteger("deployments") >= 1);
            test.assertTrue(report.getJsonObject("settings").containsKey("configurationReload"));
            test.assertTrue(report.containsKey("metrics"));
            test.assertTrue(report.getJsonObject("readiness").getBoolean("ready"));
        }));
    }

    @Test
    public void testStatusReportContainsMetricsWhenEnabled(TestContext test) {
        Configurations.system().getMetrics().setEnabled(true);
        context.metrics().registry().counter("status.test.counter").inc(7);

        deployed(test, () -> get("/status")).onComplete(test.asyncAssertSuccess(response -> {
            Configurations.system().getMetrics().setEnabled(false);
            test.assertEquals(200, response.status);
            test.assertFalse(response.json().getJsonObject("metrics").isEmpty());
        }));
    }

    @Test
    public void testStatusReportShowsShuttingDown(TestContext test) {
        deployed(test, () -> {
            context.down = true;
            return get("/status");
        }).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals("SHUTTING_DOWN", response.json().getString("state"));
            test.assertFalse(response.json().getBoolean("ready"));
        }));
    }

    @Test
    public void testPageIsServedWithAnUniqueNonce(TestContext test) {
        deployed(test, () -> get("/").compose(first -> get("/").map(second -> new Response[]{first, second})))
                .onComplete(test.asyncAssertSuccess(responses -> {
                    Response first = responses[0];
                    test.assertEquals(200, first.status);
                    test.assertTrue(first.headers.get("Content-Type").startsWith("text/html"));

                    String nonce = nonce(first);
                    String html = first.body.toString();
                    test.assertFalse(html.contains("{{nonce}}"));
                    // the script and the style carry the nonce that the policy allows.
                    test.assertTrue(html.contains("<script nonce=\"" + nonce + "\">"));
                    test.assertTrue(html.contains("<style nonce=\"" + nonce + "\">"));

                    String policy = first.headers.get("Content-Security-Policy");
                    test.assertTrue(policy.contains("default-src 'none'"));
                    test.assertTrue(policy.contains("connect-src 'self'"));
                    test.assertFalse(policy.contains("unsafe-inline"));
                    test.assertNotEquals(nonce, nonce(responses[1]));
                }));
    }

    @Test
    public void testPageLoadsNothingFromOtherSites(TestContext test) {
        deployed(test, () -> get("/")).onComplete(test.asyncAssertSuccess(response -> {
            String html = response.body.toString();
            test.assertFalse(html.contains("http://"));
            test.assertFalse(html.contains("https://"));
            test.assertFalse(html.contains("innerHTML"));
        }));
    }

    @Test
    public void testSecurityHeadersAndNoCrossOriginAccess(TestContext test) {
        deployed(test, () -> get("/status")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals("nosniff", response.headers.get("X-Content-Type-Options"));
            test.assertEquals("no-store", response.headers.get("Cache-Control"));
            test.assertEquals("DENY", response.headers.get("X-Frame-Options"));
            test.assertNull(response.headers.get("Access-Control-Allow-Origin"));
        }));
    }

    @Test
    public void testOtherMethodsAreNotAllowed(TestContext test) {
        deployed(test, () -> request(HttpMethod.POST, "/health/ready")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(405, response.status);
            test.assertEquals("GET, HEAD", response.headers.get("Allow"));
        }));
    }

    @Test
    public void testUnknownPathIsNotFound(TestContext test) {
        deployed(test, () -> get("/admin")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(404, response.status);
            test.assertEquals("not found", response.json().getString("error"));
        }));
    }

    @Test
    public void testDetailsCanBeDisabled(TestContext test) {
        service.details(false);

        deployed(test, () -> get("/status").compose(status -> get("/").compose(page ->
                get("/health/ready").map(ready -> new Response[]{status, page, ready}))))
                .onComplete(test.asyncAssertSuccess(responses -> {
                    test.assertEquals(404, responses[0].status);
                    test.assertEquals(404, responses[1].status);
                    test.assertEquals(200, responses[2].status);
                }));
    }

    @Test
    public void testServerIsClosedWhenTheServiceIsStopped(TestContext test) {
        Async async = test.async();

        deployed(test, () -> get("/health/live")).onComplete(test.asyncAssertSuccess(response -> {
            test.assertEquals(200, response.status);
            context.stop().compose(done -> get("/health/live")).onComplete(result -> {
                test.assertTrue(result.failed());
                async.complete();
            });
        }));
    }

    private String nonce(Response response) {
        Matcher matcher = NONCE.matcher(response.headers.get("Content-Security-Policy"));
        if (!matcher.find()) {
            throw new AssertionError("no nonce in " + response.headers.get("Content-Security-Policy"));
        }
        return matcher.group(1);
    }

    /**
     * Deploys the service and then runs the given requests.
     */
    private <T> Future<T> deployed(TestContext test, java.util.function.Supplier<Future<T>> requests) {
        return context.service(() -> service).compose(id -> requests.get());
    }

    private Future<Response> get(String path) {
        return request(HttpMethod.GET, path);
    }

    private Future<Response> request(HttpMethod method, String path) {
        int port = settings.getListenPorts().iterator().next();

        return client.request(method, port, LOOPBACK, path)
                .compose(request -> request.send())
                .compose(response -> response.body().map(body -> new Response(response.statusCode(), response.headers(), body)));
    }

    private record Response(int status, MultiMap headers, Buffer body) {
        JsonObject json() {
            return body.toJsonObject();
        }
    }

    /**
     * A context that can be told to be shutting down, without shutting down.
     */
    private static class ShuttingDownContext extends ContextMock {
        volatile boolean down = false;

        @Override
        public boolean isShuttingDown() {
            return down || super.isShuttingDown();
        }
    }
}
