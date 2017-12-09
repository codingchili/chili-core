package com.codingchili.core.configuration.system;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.KeyStore;
import com.codingchili.core.security.KeyStoreBuilder;
import com.codingchili.core.security.TrustAndKeyProvider;
import com.codingchili.core.testing.ContextMock;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.codingchili.core.configuration.CoreStrings.PATH_SECURITY;
import static com.codingchili.core.files.Configurations.security;

/**
 * Tests for security settings.
 */
@RunWith(VertxUnitRunner.class)
public class SecuritySettingsTest {
    private static final String KEYSTORE_JKS = CoreStrings.testFile("testkeystore.jks");
    private static final String IDENTIFIER = "defaultjks";
    private static final String PWD = "password";

    @Test
    public void readKeyStoreSettingsFromJsonFile(TestContext test) {
        getKeystoreBuilder().build().save();
        JsonObject json = ConfigurationFactory.readObject(PATH_SECURITY);
        verifyKeyStore(Serializer.unpack(json, SecuritySettings.class), test);
    }

    @Test
    public void addKeyStoreWithBuilder(TestContext test) {
        getKeystoreBuilder().build();
        verifyKeyStore(security(), test);
    }

    @Test
    public void testLoadKeyStore(TestContext test) {
        ContextMock mock = new ContextMock();
        try {
            getKeystoreBuilder().build();
            TrustAndKeyProvider provider = security().getKeystore(mock, IDENTIFIER);
            test.assertNotNull(provider);
        } finally {
            mock.close();
        }
    }

    private KeyStoreBuilder<SecuritySettings> getKeystoreBuilder() {
        return security().addKeystore()
                .setShortName(IDENTIFIER)
                .setPassword(PWD)
                .setPath(KEYSTORE_JKS);
    }

    private void verifyKeyStore(SecuritySettings settings, TestContext test) {
        test.assertTrue(security().getKeystores().containsKey(IDENTIFIER));
        KeyStore store = settings.getKeystores().get(IDENTIFIER);
        test.assertEquals(KEYSTORE_JKS, store.getPath());
        test.assertEquals(IDENTIFIER, store.getShortName());
        test.assertEquals(PWD, store.getPassword());
    }
}