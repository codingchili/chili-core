package com.codingchili.core.security;

import io.vertx.core.net.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * A self-signed certificate for development and test, as {@link io.vertx.core.net.SelfSignedCertificate}
 * but with access to the underlying certificate, for the public and private key.
 * <p>
 * The certificate is valid for the given name, for localhost and for the loopback addresses, and for a year. It is
 * created using only the JDK, see {@link SelfSignedCertificates}. The key and the certificate are written
 * as PEM files that are deleted when the JVM exits.
 */
public class TestCertificate implements SelfSignedCertificate {
    private static final int KEY_SIZE = 2048;
    private final X509Certificate certificate;
    private final PrivateKey key;
    private final File keyFile;
    private final File certificateFile;

    /**
     * Creates a new self signed certificate using the provided fqdn.
     *
     * @param fqdn of the certificate to generate.
     */
    public TestCertificate(String fqdn) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(KEY_SIZE);
            KeyPair keys = generator.generateKeyPair();

            this.key = keys.getPrivate();
            this.certificate = SelfSignedCertificates.create(fqdn, keys);
            this.keyFile = write("key", "PRIVATE KEY", key.getEncoded());
            this.certificateFile = write("cert", "CERTIFICATE", certificate.getEncoded());
        } catch (GeneralSecurityException | IOException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    private static File write(String type, String label, byte[] der) throws IOException {
        File file = Files.createTempFile("chili-" + type + "-", ".pem").toFile();
        file.deleteOnExit();

        // the key is only readable by the owner, where the platform supports it.
        file.setReadable(false, false);
        file.setReadable(true, true);

        String pem = "-----BEGIN " + label + "-----\n" +
                Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(der) +
                "\n-----END " + label + "-----\n";
        Files.writeString(file.toPath(), pem, StandardCharsets.US_ASCII);
        return file;
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
        return keyFile.getAbsolutePath();
    }

    @Override
    public String certificatePath() {
        return certificateFile.getAbsolutePath();
    }

    @Override
    public void delete() {
        keyFile.delete();
        certificateFile.delete();
    }

    /**
     * @return the certificate.
     */
    public X509Certificate getCertificate() {
        return certificate;
    }

    /**
     * @return the public key of this certificate.
     */
    public PublicKey getPublicKey() {
        return certificate.getPublicKey();
    }

    /**
     * @return the private key of this certificate.
     */
    public PrivateKey getPrivateKey() {
        return key;
    }
}
