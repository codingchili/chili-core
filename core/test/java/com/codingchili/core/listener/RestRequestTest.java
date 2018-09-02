package com.codingchili.core.listener;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.auth.User;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.*;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Locale;
import io.vertx.ext.web.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.util.*;

import com.codingchili.core.listener.transport.RestRequest;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_ROUTE;
import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_TARGET;

/**
 * @author Robin Duda
 *
 * Tests to verify that parsing of HTTP target/route representations are working as expected.
 */
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
    private JsonObject BODY_ROUTE = new JsonObject()
            .put(PROTOCOL_TARGET, TARGET)
            .put(PROTOCOL_ROUTE, ROUTE)
            .put(PAYLOAD, JESUS);

    @Test
    public void stripBasePath(TestContext test) {
        RestRequest.setBasePath(BASEPATH);
        assertRoute(getRequest(BASEPATH + PATH_ROUTE), test);
        RestRequest.setBasePath(null);
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
        RestRequest.setBasePath(BASEPATH);
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

    private class RoutingContextMock implements RoutingContext {
        private Buffer body;
        private String path;

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
                public String rawMethod() {
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
                public String host() {
                    return null;
                }

                @Override
                public HttpServerResponse response() {
                    return null;
                }

                @Override
                public MultiMap headers() {
                    return new CaseInsensitiveHeaders();
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
                public MultiMap params() {
                    MultiMap map = new CaseInsensitiveHeaders();

                    if (path.contains("?")) {
                        String qs = path.substring(path.lastIndexOf("?"), path.length());

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
                public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
                    return new X509Certificate[0];
                }

                @Override
                public String absoluteURI() {
                    return null;
                }

                @Override
                public NetSocket netSocket() {
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
                public ServerWebSocket upgrade() {
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
        public RoutingContext put(String s, Object o) {
            return null;
        }

        @Override
        public <T> T get(String s) {
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
        public String normalisedPath() {
            return null;
        }

        @Override
        public Cookie getCookie(String s) {
            return null;
        }

        @Override
        public RoutingContext addCookie(Cookie cookie) {
            return null;
        }

        @Override
        public Cookie removeCookie(String s) {
            return null;
        }

        @Override
        public int cookieCount() {
            return 0;
        }

        @Override
        public Set<Cookie> cookies() {
            return null;
        }

        @Override
        public String getBodyAsString() {
            return null;
        }

        @Override
        public String getBodyAsString(String s) {
            return null;
        }

        @Override
        public JsonObject getBodyAsJson() {
            return null;
        }

        @Override
        public JsonArray getBodyAsJsonArray() {
            return null;
        }

        @Override
        public Buffer getBody() {
            return body;
        }

        @Override
        public Set<FileUpload> fileUploads() {
            return null;
        }

        @Override
        public Session session() {
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
        public boolean failed() {
            return false;
        }

        @Override
        public void setBody(Buffer buffer) {

        }

        @Override
        public void setSession(Session session) {

        }

        @Override
        public void setUser(User user) {

        }

        @Override
        public void clearUser() {

        }

        @Override
        public void setAcceptableContentType(String s) {

        }

        @Override
        public void reroute(HttpMethod httpMethod, String s) {

        }

        @Override
        public List<Locale> acceptableLocales() {
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
        public List<String> queryParam(String s) {
            return null;
        }
    }
}
