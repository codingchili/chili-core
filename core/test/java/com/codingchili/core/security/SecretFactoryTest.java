package com.codingchili.core.security;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Base64;
import java.util.HashSet;

/**
 * @author Robin Duda
 *
 * Tests the generation and verification of secret keys in SecretFactory
 */
@RunWith(VertxUnitRunner.class)
public class SecretFactoryTest {

    @Test
    public void testSecretsAreUnique(TestContext test) {
        HashSet<String> secrets = new HashSet<>();

        for (int i = 0; i < 55256; i++) {
            String secret = SecretFactory.generate(64);

            if (secrets.contains(secret)) {
                test.fail("Generated non-unique token: " + secret);
                break;
            }
            secrets.add(secret);
        }
    }

    @Test
    public void testSecretEntropy(TestContext test) {
        String secret = SecretFactory.generate(512);
        test.assertEquals(512, Base64.getDecoder().decode(secret).length);
    }

    @Test
    public void testVerifySecret(TestContext test) {
        String secret = SecretFactory.generate(1024);
        test.assertTrue(SecretFactory.verify(secret, secret));
    }

    @Test
    public void testVerifyFailingSecret(TestContext test) {
        String secret = SecretFactory.generate(1024);

        for (int i = 0; i < secret.length(); i++) {
            byte[] copy = secret.getBytes();
            copy[i] ^= secret.getBytes()[i];
            String mismatch = new String(copy);
            test.assertFalse(SecretFactory.verify(secret, mismatch));
        }
    }
}
