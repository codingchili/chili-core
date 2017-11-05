package com.codingchili.core.security;

import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.core.net.TrustOptions;

/**
 * Common wrapper class for #{@link SelfSignedCertificate} and #{@link JksOptions} as they
 * both contain trustOptions and keyCertOptions but these methods are not contained in
 * a common interface.
 */
public class TrustAndKeyProvider implements KeyCertOptions, TrustOptions {
    private TrustOptions trust;
    private KeyCertOptions keyCert;

    public TrustAndKeyProvider(TrustOptions trust, KeyCertOptions keyCert) {
        this.trust = trust;
        this.keyCert = keyCert;
    }

    /**
     * @param self a self signed certificate to get trust and keycert options from.
     * @return a new TrustAndKeyProvider instance.
     */
    public static TrustAndKeyProvider of(SelfSignedCertificate self) {
        return new TrustAndKeyProvider(self.trustOptions(), self.keyCertOptions());
    }

    /**
     * @param jks a java keystore to get trust and keycert options from.
     * @return a new TrustAndKeyProvider instance.
     */
    public static TrustAndKeyProvider of(JksOptions jks) {
        return new TrustAndKeyProvider(jks, jks);
    }

    /**
     * @return trust options for the wrapped provider implementation.
     */
    public TrustOptions trustOptions() {
        return trust;
    }

    /**
     * @return key and cert options for the wrapped provider implementation.
     */
    public KeyCertOptions keyCertOptions() {
        return keyCert;
    }

    @Override
    public TrustAndKeyProvider clone() {
        return new TrustAndKeyProvider(trust, keyCert);
    }
}
