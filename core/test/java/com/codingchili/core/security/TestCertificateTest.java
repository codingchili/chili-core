package com.codingchili.core.security;

import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.core.net.ServerSSLOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The self-signed certificate that is generated when no keystore is configured.
 */
@RunWith(VertxUnitRunner.class)
public class TestCertificateTest {
    private static final int DNS = 2;
    private static final int IP = 7;
    private final Vertx vertx = Vertx.vertx();
    private TestCertificate generated;

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.SECONDS);

    @After
    public void tearDown(TestContext test) {
        if (generated != null) {
            generated.delete();
        }
        vertx.close().onComplete(test.asyncAssertSuccess());
    }

    @Test
    public void testCertificateIsSelfSignedAndValid() throws Exception {
        generated = new TestCertificate("service.example.com");
        X509Certificate certificate = generated.getCertificate();

        Assert.assertEquals("CN=service.example.com", certificate.getSubjectX500Principal().getName());
        Assert.assertEquals(certificate.getSubjectX500Principal(), certificate.getIssuerX500Principal());
        Assert.assertEquals(3, certificate.getVersion());
        Assert.assertEquals("SHA256withRSA", certificate.getSigAlgName());
        Assert.assertEquals("RSA", certificate.getPublicKey().getAlgorithm());

        // signed with the key that belongs to the certificate: throws if not.
        certificate.verify(generated.getPublicKey());
        certificate.checkValidity(new Date());
        Assert.assertTrue(certificate.getNotAfter().getTime() - System.currentTimeMillis() > TimeUnit.DAYS.toMillis(300));
    }

    @Test
    public void testNotACertificateAuthorityAndUsableForServers() throws Exception {
        generated = new TestCertificate("localhost");
        X509Certificate certificate = generated.getCertificate();

        // -1: not a certificate authority.
        Assert.assertEquals(-1, certificate.getBasicConstraints());
        Assert.assertTrue(certificate.getExtendedKeyUsage().contains("1.3.6.1.5.5.7.3.1"));
    }

    @Test
    public void testValidForTheHostLocalhostAndTheLoopbackAddresses() throws Exception {
        generated = new TestCertificate("service.example.com");

        List<String> dns = names(generated.getCertificate(), DNS);
        List<String> addresses = names(generated.getCertificate(), IP);

        Assert.assertTrue(dns.contains("service.example.com"));
        Assert.assertTrue(dns.contains("localhost"));
        Assert.assertTrue(addresses.contains("127.0.0.1"));
        Assert.assertTrue(addresses.contains("0:0:0:0:0:0:0:1"));
    }

    @Test
    public void testNameThatIsNotAHostIsNotAnAlternativeName() throws Exception {
        // any name may be the subject: this is not something that a client can verify a host against.
        generated = new TestCertificate("https://github.com/codingchili");

        Assert.assertEquals("CN=https://github.com/codingchili",
                generated.getCertificate().getSubjectX500Principal().getName());
        Assert.assertEquals(List.of("localhost"), names(generated.getCertificate(), DNS));
    }

    @Test
    public void testEachCertificateHasItsOwnKeysAndSerial() {
        TestCertificate first = new TestCertificate("localhost");
        TestCertificate second = new TestCertificate("localhost");
        try {
            Assert.assertNotEquals(first.getPublicKey(), second.getPublicKey());
            Assert.assertNotEquals(first.getCertificate().getSerialNumber(), second.getCertificate().getSerialNumber());
        } finally {
            first.delete();
            second.delete();
        }
    }

    @Test
    public void testPemFilesCanBeReadBack() throws Exception {
        generated = new TestCertificate("localhost");

        String certificate = Files.readString(Paths.get(generated.certificatePath()), StandardCharsets.US_ASCII);
        X509Certificate read = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(certificate.getBytes(StandardCharsets.US_ASCII)));
        Assert.assertEquals(generated.getCertificate(), read);

        String key = Files.readString(Paths.get(generated.privateKeyPath()), StandardCharsets.US_ASCII);
        Assert.assertTrue(key.startsWith("-----BEGIN PRIVATE KEY-----"));
        byte[] der = Base64.getMimeDecoder().decode(key
                .replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", ""));
        Assert.assertEquals(generated.getPrivateKey(),
                KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der)));
    }

    @Test
    public void testFilesAreDeleted() {
        generated = new TestCertificate("localhost");
        generated.delete();

        Assert.assertFalse(Paths.get(generated.certificatePath()).toFile().exists());
        Assert.assertFalse(Paths.get(generated.privateKeyPath()).toFile().exists());
    }

    @Test
    public void testTlsHandshakeWithHostNameVerification(TestContext test) {
        generated = new TestCertificate("localhost");
        Async async = test.async();

        HttpServer server = vertx.createHttpServer(new HttpServerConfig(),
                new ServerSSLOptions().setKeyCertOptions(generated.keyCertOptions()));
        server.requestHandler(request -> request.response().end("secure"));

        HttpClientAgent client = vertx.createHttpClient(new HttpClientConfig().setSsl(true), new ClientSSLOptions()
                .setTrustOptions(generated.trustOptions())
                .setHostnameVerificationAlgorithm("HTTPS"));

        server.listen(0, "127.0.0.1").compose(bound -> {
            // verified against the IP address and the name, both are alternative names of the certificate.
            return get(client, bound.actualPort(), "127.0.0.1").compose(first ->
                    get(client, bound.actualPort(), "localhost").map(second -> first + second));
        }).onComplete(test.asyncAssertSuccess(body -> {
            test.assertEquals("securesecure", body);
            async.complete();
        }));
    }

    @Test
    public void testClientThatDoesNotTrustTheCertificateIsRejected(TestContext test) {
        generated = new TestCertificate("localhost");

        HttpServer server = vertx.createHttpServer(new HttpServerConfig(),
                new ServerSSLOptions().setKeyCertOptions(generated.keyCertOptions()));
        server.requestHandler(request -> request.response().end("secure"));

        // the default trust store does not contain a self-signed certificate.
        HttpClientAgent client = vertx.createHttpClient(new HttpClientConfig().setSsl(true), new ClientSSLOptions());

        server.listen(0, "127.0.0.1")
                .compose(bound -> get(client, bound.actualPort(), "127.0.0.1"))
                .onComplete(test.asyncAssertFailure());
    }

    private static io.vertx.core.Future<String> get(HttpClientAgent client, int port, String host) {
        return client.request(HttpMethod.GET, port, host, "/")
                .compose(request -> request.send())
                .compose(response -> response.body())
                .map(body -> body.toString());
    }

    /**
     * @param type 2 for DNS names, 7 for IP addresses.
     */
    private static List<String> names(X509Certificate certificate, int type) throws Exception {
        Collection<List<?>> alternatives = certificate.getSubjectAlternativeNames();
        return alternatives.stream()
                .filter(name -> (Integer) name.get(0) == type)
                .map(name -> (String) name.get(1))
                .collect(Collectors.toList());
    }
}
