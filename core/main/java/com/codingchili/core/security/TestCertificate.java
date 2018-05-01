package com.codingchili.core.security;

import io.vertx.core.net.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Copied from {@link io.vertx.core.net.SelfSignedCertificate} because we need to access
 * the underlying certificate, for the public and private key.
 */
public class TestCertificate implements SelfSignedCertificate {
    private final io.netty.handler.ssl.util.SelfSignedCertificate certificate;

    /**
     * Creates a new self signed certificate using the provided fqdn.
     *
     * @param fqdn of the certificate to generate.
     */
    public TestCertificate(String fqdn) {
        try {
            this.certificate = new io.netty.handler.ssl.util.SelfSignedCertificate(fqdn);
        } catch (CertificateException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @Override
    public PemKeyCertOptions keyCertOptions() {
        return (new PemKeyCertOptions()).setKeyPath(this.privateKeyPath()).setCertPath(this.certificatePath());
    }

    @Override
    public PemTrustOptions trustOptions() {
        return (new PemTrustOptions()).addCertPath(this.certificatePath());
    }

    @Override
    public String privateKeyPath() {
        return this.certificate.privateKey().getAbsolutePath();
    }

    @Override
    public String certificatePath() {
        return this.certificate.certificate().getAbsolutePath();
    }

    @Override
    public void delete() {
        this.certificate.delete();
    }

    /**
     * @return the public key of this certificate.
     */
    public PublicKey getPublicKey() {
        return certificate.cert().getPublicKey();
    }

    /**
     * @return the private key of this certificate.
     */
    public PrivateKey getPrivateKey() {
        return certificate.key();
    }
}
