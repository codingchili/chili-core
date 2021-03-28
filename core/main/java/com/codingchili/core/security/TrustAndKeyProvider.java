package com.codingchili.core.security;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * Common wrapper class for #{@link SelfSignedCertificate} and #{@link JksOptions} as they
 * both contain trustOptions and keyCertOptions but these methods are not contained in
 * a common interface.
 */
public class TrustAndKeyProvider {
    private TrustOptions trust;
    private KeyCertOptions keyCert;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String path = "<self-signed>";
    private String alias = "<self-signed>";

    /**
     * Creates a new trust/key provider using the given jks options.
     *
     * @param reference options with trust and/or key configuration.
     */
    private TrustAndKeyProvider(KeyStoreReference reference) {
        this.trust = reference;
        this.keyCert = reference;
        this.path = reference.getPath();
        try {
            KeyStore store = KeyStore.getInstance(reference.getType());

            // load the keystore from disk.
            reference.setValue(Buffer.buffer(
                    Files.readAllBytes(Paths.get(reference.getPath()).toAbsolutePath()))
            );

            // load the keystore, will be loaded twice - this is to be able to grab a reference to the keypair.
            // the other load is internal to KeyStoreOptionsBase/KeyStoreHelper.
            store.load(new ByteArrayInputStream(reference.getValue().getBytes()), reference.getPassword().toCharArray());

            // only support one alias per keystore.
            Enumeration<String> aliases = store.aliases();
            if (aliases.hasMoreElements()) {
                alias = aliases.nextElement();
                if (aliases.hasMoreElements()) {
                    // ensure that the single alias is chosen deterministically.
                    throw new IllegalStateException(CoreStrings.getKeystoreTooManyEntries(reference.getPath()));
                }
            } else {
                throw new NoSuchElementException(CoreStrings.getEmptyKeyStore(reference.getPath()));
            }

            // attempt to load the public key if available.
            if (store.isCertificateEntry(alias)) {
                this.publicKey = store.getCertificate(alias).getPublicKey();
            }

            // attempt to load the private key if available.
            if (store.isKeyEntry(alias)) {
                this.publicKey = store.getCertificate(alias).getPublicKey();

                // we re-use the jks password for the private key.
                this.privateKey = (PrivateKey) store.getKey(alias, reference.getPassword().toCharArray());
            }
        } catch (UnrecoverableKeyException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    /**
     * Creates a new trust/key provider using the given trust and key options.
     *
     * @param certificate a self signed certificate.
     */
    private TrustAndKeyProvider(TestCertificate certificate) {
        this.trust = certificate.trustOptions();
        this.keyCert = certificate.keyCertOptions();
        this.publicKey = certificate.getPublicKey();
        this.privateKey = certificate.getPrivateKey();
    }

    /**
     * @return the private key of this keystore.
     */
    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            throw new CoreRuntimeException(
                    String.format("there is no private key with alias '%s' in keystore '%s'.",
                            alias,
                            path
                    )
            );
        }
        return privateKey;
    }

    /**
     * @return the public key of this keystore.
     */
    public PublicKey getPublicKey() {
        if (publicKey == null) {
            throw new CoreRuntimeException(
                    String.format("there is no public key with alias '%s' in keystore '%s'.",
                            alias,
                            path
                    )
            );
        }
        return publicKey;
    }

    /**
     * @param self a self signed certificate to get trust and keycert options from.
     * @return a new TrustAndKeyProvider instance.
     */
    public static TrustAndKeyProvider of(TestCertificate self) {
        return new TrustAndKeyProvider(self);
    }

    /**
     * @param jks a java keystore to get trust and keycert options from.
     * @return a new TrustAndKeyProvider instance.
     */
    public static TrustAndKeyProvider of(KeyStoreReference jks) {
        return new TrustAndKeyProvider(jks);
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
}
