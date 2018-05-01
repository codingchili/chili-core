package com.codingchili.core.configuration.system;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.*;
import com.codingchili.core.testing.ContextMock;

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.PATH_SECURITY;
import static com.codingchili.core.files.Configurations.security;

/**
 * Tests for security settings.
 */
@RunWith(VertxUnitRunner.class)
public class SecuritySettingsTest {
    private static final String KEY_JKS = "test_key.jks";
    private static final String KEYSTORE_JKS = CoreStrings.testFile(KEY_JKS);
    private static final String TRUST_JKS = "test_trust.jks";
    private static final String TRUSTSTORE_JKS = CoreStrings.testFile(TRUST_JKS);
    private static final String IDENTIFIER = "defaultjks";
    private static final String ALGORITHM_RSA = "RSA";
    private static final String PWD = "password";
    private CoreContext context;

    @Before
    public void setUp() {
        context = new ContextMock();
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void readKeyStoreSettingsFromJsonFile(TestContext test) {
        getKeyStoreBuilder(KEYSTORE_JKS).build().save();
        JsonObject json = ConfigurationFactory.readObject(PATH_SECURITY);
        verifyKeyStore(Serializer.unpack(json, SecuritySettings.class), test);
    }

    @Test
    public void addKeyStoreWithBuilder(TestContext test) {
        getKeyStoreBuilder(KEYSTORE_JKS).build();
        verifyKeyStore(security(), test);
    }

    @Test
    public void loadKeysFromSelfSigned(TestContext test) {
        TrustAndKeyProvider provider = TrustAndKeyProvider.of(new TestCertificate("fqdn"));
        test.assertEquals(provider.getPrivateKey().getAlgorithm(), ALGORITHM_RSA);
        test.assertEquals(provider.getPublicKey().getAlgorithm(), ALGORITHM_RSA);
    }

    @Test
    public void loadKeyStoreWithPrivateKey(TestContext test) {
        getKeyStoreBuilder(KEYSTORE_JKS).build();
        TrustAndKeyProvider provider = security().getKeystore(context, KEY_JKS);

        // verify that the private key can be loaded.
        test.assertEquals(provider.getPrivateKey().getAlgorithm(), ALGORITHM_RSA);

        // verify that the public key is loaded - if we know the private key
        // the public key should always be available in the keystore as well.
        test.assertEquals(provider.getPublicKey().getAlgorithm(), ALGORITHM_RSA);
    }

    @Test
    public void loadKeyStoreWithPublicKey(TestContext test) {
        getKeyStoreBuilder(TRUSTSTORE_JKS).build();
        TrustAndKeyProvider provider = security().getKeystore(context, TRUST_JKS);
        test.assertEquals(provider.getPublicKey().getAlgorithm(), ALGORITHM_RSA);
        try {
            // the private key is not available - access will throw an exception.
            provider.getPrivateKey();
            test.fail("error: retrieved private key from truststore.");
        } catch (Exception e) {
            // expected - private key not available in truststore.
        }
    }

    private KeyStoreBuilder<SecuritySettings> getKeyStoreBuilder(String path) {
        return security().addKeystore()
                .setPassword(PWD)
                .setPath(path);
    }

    private void verifyKeyStore(SecuritySettings settings, TestContext test) {
        test.assertTrue(security().getByName(KEY_JKS).isPresent());
        KeyStoreReference store = settings.getByName(KEY_JKS).get();
        test.assertEquals(KEYSTORE_JKS, store.getPath());
        test.assertEquals(KEY_JKS, store.getShortName());
        test.assertEquals(PWD, store.getPassword());
    }
}