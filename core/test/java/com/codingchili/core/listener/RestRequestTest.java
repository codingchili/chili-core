package com.codingchili.core.listener;

import com.codingchili.core.listener.transport.RestRequest;
import io.netty.handler.codec.DecoderResult;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.HostAndPort;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.HostAndPortImpl;
import io.vertx.ext.auth.User;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.*;
import io.vertx.ext.web.Session;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLSession;
import java.nio.charset.Charset;
import java.util.*;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_ROUTE;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_TARGET;

/**
 * Tests to verify that parsing of HTTP target/route representations are working as expected.
 */
@SuppressWarnings("deprecation")
@RunWith(VertxUnitRunner.class)
public class RestRequestTest {
    private static final String DEFAULT_TARGET = "defaultTarget";
    private static final ListenerSettings settings = new ListenerSettings().setDefaultTarget(DEFAULT_TARGET);
    private static final String JESUS = "jesus";
    private static final String PAYLOAD = "payload";
    private static final String BASEPATH = "/api/rest/2.0";
    private static final String PATH_ROUTE = "/accounts/view/history";
    private static final String QUERYSTRING_ROUTE = "?target=accounts&route=view/history";
    private static final String TARGET = "accounts";
    private static final String ROUTE = "view/history";
    private final JsonObject BODY_ROUTE = new JsonObject()
            .put(PROTOCOL_TARGET, TARGET)
            .put(PROTOCOL_ROUTE, ROUTE)
            .put(PAYLOAD, JESUS);

    @After
    public void tearDown() {
        settings.setBasePath(null);
    }

    @Test
    public void stripBasePath(TestContext test) {
        settings.setBasePath(BASEPATH);
        assertRoute(getRequest(BASEPATH + PATH_ROUTE), test);
    }

    @Test
    public void readRouteFromPath(TestContext test) {
        assertRoute(getRequest(QUERYSTRING_ROUTE), test);
    }

    @Test
    public void readRouteFromQueryString(TestContext test) {
        assertRoute(getRequest(BASEPATH + QUERYSTRING_ROUTE), test);
    }

    @Test
    public void readRouteFromBody(TestContext test) {
        assertRoute(getRequest(BODY_ROUTE), test);
    }

    @Test
    public void mergeQueryParamsWithBody(TestContext test) {
        RestRequest request = new RestRequest(new RoutingContextMock(PATH_ROUTE, BODY_ROUTE), settings);
        JsonObject data = request.data();
        test.assertTrue(data.containsKey(PAYLOAD));
        test.assertEquals(data.getString(PAYLOAD), JESUS);
    }

    @Test
    public void setDefaultTargetIfTargetNotSpecified(TestContext test) {
        settings.setBasePath(BASEPATH);
        test.assertEquals(getRequest(BASEPATH).target(), DEFAULT_TARGET);
    }

    private void assertRoute(RestRequest request, TestContext test) {
        test.assertEquals(TARGET, request.target());
        test.assertEquals(ROUTE, request.route());
    }

    private RestRequest getRequest(JsonObject json) {
        return new RestRequest(new RoutingContextMock(json), settings);
    }

    private RestRequest getRequest(String path) {
        return new RestRequest(new RoutingContextMock(path), settings);
    }

    private static class RoutingContextMock implements RoutingContext {
        protected final Buffer body;
        private final String path;

        /**
         * @param path the path that will be returned by request().path().
         */
        public RoutingContextMock(String path) {
            this(path, new JsonObject());
        }

        /**
         * @param body the payload that will be returned by request().body().
         */
        public RoutingContextMock(JsonObject body) {
            this("", body);
        }

        /**
         * @param path the path that will be returned by request().path().
         * @param body the payload that will be returned by request().body().
         */
        public RoutingContextMock(String path, JsonObject body) {
            this.body = body.toBuffer();
            this.path = path;
        }

        @Override
        public HttpServerRequest request() {
            return new HttpServerRequest() {
                @Override
                public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
                    return null;
                }

                @Override
                public HttpServerRequest handler(Handler<Buffer> handler) {
                    return null;
                }

                @Override
                public HttpServerRequest pause() {
                    return null;
                }

                @Override
                public HttpServerRequest resume() {
                    return null;
                }

                @Override
                public HttpServerRequest fetch(long amount) {
                    return this;
                }

                @Override
                public HttpServerRequest endHandler(Handler<Void> handler) {
                    return null;
                }

                @Override
                public HttpVersion version() {
                    return null;
                }

                @Override
                public HttpMethod method() {
                    return null;
                }

                @Override
                public boolean isSSL() {
                    return false;
                }

                @Override
                public String scheme() {
                    return null;
                }

                @Override
                public String uri() {
                    return null;
                }

                @Override
                public String path() {
                    return path;
                }

                @Override
                public String query() {
                    return null;
                }

                @Override
                public HostAndPort authority() {
                    return new HostAndPortImpl("none", 21);
                }

                @Override
                public HostAndPort authority(boolean real) {
                    return null;
                }

                @Override
                public long bytesRead() {
                    return 0;
                }

                @Override
                public HttpServerResponse response() {
                    return null;
                }

                @Override
                public MultiMap headers() {
                    return HttpHeaders.headers();
                }

                @Override
                public String getHeader(String s) {
                    return null;
                }

                @Override
                public String getHeader(CharSequence charSequence) {
                    return null;
                }

                @Override
                public HttpServerRequest setParamsCharset(String charset) {
                    return null;
                }

                @Override
                public String getParamsCharset() {
                    return null;
                }

                @Override
                public MultiMap params() {
                    MultiMap map = HttpHeaders.headers();

                    if (path.contains("?")) {
                        String qs = path.substring(path.lastIndexOf("?"));

                        Arrays.stream(qs.split("([?&])"))
                                .map(line -> line.split("="))
                                .filter(pair -> pair.length > 1)
                                .map(keys -> new AbstractMap.SimpleEntry<>(keys[0], keys[1]))
                                .forEach(pair -> {
                                    map.add(pair.getKey(), pair.getValue());
                                });
                    }

                    return map;
                }

                @Override
                public MultiMap params(boolean semicolonIsNormalChar) {
                    return null;
                }

                @Override
                public String getParam(String s) {
                    return null;
                }

                @Override
                public SocketAddress remoteAddress() {
                    return null;
                }

                @Override
                public SocketAddress localAddress() {
                    return null;
                }

                @Override
                public SSLSession sslSession() {
                    return null;
                }

                @Override
                public String absoluteURI() {
                    return null;
                }

                @Override
                public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
                    return null;
                }


                @Override
                public Future<Buffer> body() {
                    return Future.succeededFuture(body);
                }


                @Override
                public Future<Void> end() {
                    return null;
                }


                @Override
                public Future<NetSocket> toNetSocket() {
                    return null;
                }

                @Override
                public HttpServerRequest setExpectMultipart(boolean b) {
                    return null;
                }

                @Override
                public boolean isExpectMultipart() {
                    return false;
                }

                @Override
                public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> handler) {
                    return null;
                }

                @Override
                public MultiMap formAttributes() {
                    return null;
                }

                @Override
                public String getFormAttribute(String s) {
                    return null;
                }


                @Override
                public Future<ServerWebSocket> toWebSocket() {
                    return null;
                }

                @Override
                public boolean isEnded() {
                    return false;
                }

                @Override
                public HttpServerRequest customFrameHandler(Handler<HttpFrame> handler) {
                    return null;
                }

                @Override
                public HttpConnection connection() {
                    return null;
                }

                @Override
                public StreamPriority streamPriority() {
                    return null;
                }

                @Override
                public HttpServerRequest streamPriorityHandler(Handler<StreamPriority> handler) {
                    return this;
                }

                @Override
                public DecoderResult decoderResult() {
                    return null;
                }

                @Override
                public Cookie getCookie(String name) {
                    return null;
                }

                @Override
                public Cookie getCookie(String name, String domain, String path) {
                    return null;
                }

                @Override
                public int cookieCount() {
                    return 0;
                }

                @Override
                public Set<Cookie> cookies(String name) {
                    return null;
                }

                @Override
                public Set<Cookie> cookies() {
                    return null;
                }

                @Override
                public HttpServerRequest routed(String route) {
                    return null;
                }
            };
        }

        @Override
        public HttpServerResponse response() {
            return null;
        }

        @Override
        public void next() {

        }

        @Override
        public void fail(int i) {

        }

        @Override
        public void fail(Throwable throwable) {

        }

        @Override
        public void fail(int i, Throwable throwable) {

        }

        @Override
        public RoutingContext put(String s, Object o) {
            return null;
        }

        @Override
        public <T> T get(String s) {
            return null;
        }

        @Override
        public <T> T get(String key, T defaultValue) {
            return null;
        }

        @Override
        public <T> T remove(String s) {
            return null;
        }

        @Override
        public Map<String, Object> data() {
            return null;
        }

        @Override
        public Vertx vertx() {
            return null;
        }

        @Override
        public String mountPoint() {
            return null;
        }

        @Override
        public Route currentRoute() {
            return null;
        }


        @Override
        public String normalizedPath() {
            return null;
        }

        public Buffer getBody() {
            return body;
        }

        @Override
        public RequestBody body() {
            return new RequestBody() {
                @Override
                public String asString() {
                    return body.toString();
                }

                @Override
                public String asString(String encoding) {
                    return body.toString(encoding);
                }

                @Override
                public JsonObject asJsonObject(int maxAllowedLength) {
                    return body.length() == 0 ? null : body.toJsonObject();
                }

                @Override
                public JsonArray asJsonArray(int maxAllowedLength) {
                    return body.length() == 0 ? null : body.toJsonArray();
                }

                @Override
                public Buffer buffer() {
                    return body;
                }

                @Override
                public <R> R asPojo(Class<R> type, int maxAllowedLength) {
                    throw new UnsupportedOperationException("POJO mapping requires Jackson 2, use the Serializer.");
                }

                @Override
                public int length() {
                    return body.length();
                }

                @Override
                public boolean available() {
                    return true;
                }
            };
        }

        @Override
        public List<FileUpload> fileUploads() {
            return null;
        }

        @Override
        public void cancelAndCleanupFileUploads() {

        }

        @Override
        public Session session() {
            return null;
        }

        @Override
        public boolean isSessionAccessed() {
            return false;
        }

        @Override
        public UserContext userContext() {
            return null;
        }

        @Override
        public User user() {
            return null;
        }

        @Override
        public Throwable failure() {
            return null;
        }

        @Override
        public int statusCode() {
            return 0;
        }

        @Override
        public String getAcceptableContentType() {
            return null;
        }

        @Override
        public ParsedHeaderValues parsedHeaders() {
            return null;
        }

        @Override
        public int addHeadersEndHandler(Handler<Void> handler) {
            return 0;
        }

        @Override
        public boolean removeHeadersEndHandler(int i) {
            return false;
        }

        @Override
        public int addBodyEndHandler(Handler<Void> handler) {
            return 0;
        }

        @Override
        public boolean removeBodyEndHandler(int i) {
            return false;
        }

        @Override
        public int addEndHandler(Handler<AsyncResult<Void>> handler) {
            return 0;
        }

        @Override
        public boolean removeEndHandler(int handlerID) {
            return false;
        }

        @Override
        public boolean failed() {
            return false;
        }

        @Override
        public void setAcceptableContentType(String s) {

        }

        @Override
        public void reroute(String path) {

        }

        @Override
        public void reroute(HttpMethod httpMethod, String s) {

        }

        @Override
        public List<LanguageHeader> acceptableLanguages() {
            return null;
        }

        @Override
        public LanguageHeader preferredLanguage() {
            return null;
        }

        @Override
        public Map<String, String> pathParams() {
            return null;
        }

        @Override
        public String pathParam(String s) {
            return null;
        }

        @Override
        public MultiMap queryParams() {
            return null;
        }

        @Override
        public MultiMap queryParams(Charset encoding) {
            return null;
        }

        @Override
        public List<String> queryParam(String s) {
            return null;
        }
    }
}
