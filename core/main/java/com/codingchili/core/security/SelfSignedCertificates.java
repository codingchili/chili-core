package com.codingchili.core.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Creates self-signed X.509 certificates for development and test, using nothing but the JDK.
 * <p>
 * Netty's generator requires JDK internals that have been removed, or BouncyCastle, and the JDK has no public API for
 * creating a certificate. This encodes the few ASN.1 structures that are needed (RFC 5280), and lets the JDK
 * sign the certificate, and parse it again to make sure that it is valid.
 * <p>
 * The certificate is valid for the host name, for {@code localhost}, and for the loopback addresses,
 * so that a client that verifies host names can connect to a server on the local machine.
 */
final class SelfSignedCertificates {
    // RFC 1123 host names: the certificate name may be anything, but only a host name can be an alternative name.
    private static final Pattern HOSTNAME = Pattern.compile("[A-Za-z0-9]([A-Za-z0-9.-]{0,251}[A-Za-z0-9])?");
    private static final DateTimeFormatter UTC_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss'Z'");
    private static final DateTimeFormatter GENERALIZED_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'");
    private static final int VALID_DAYS = 365;

    private static final String OID_SHA256_WITH_RSA = "1.2.840.113549.1.1.11";
    private static final String OID_COMMON_NAME = "2.5.4.3";
    private static final String OID_BASIC_CONSTRAINTS = "2.5.29.19";
    private static final String OID_EXTENDED_KEY_USAGE = "2.5.29.37";
    private static final String OID_SUBJECT_ALT_NAME = "2.5.29.17";
    private static final String OID_SERVER_AUTH = "1.3.6.1.5.5.7.3.1";
    private static final String OID_CLIENT_AUTH = "1.3.6.1.5.5.7.3.2";

    private SelfSignedCertificates() {
    }

    /**
     * @param name the common name of the certificate, usually the name of the host.
     * @param keys an RSA key pair, the certificate is signed with the private key.
     * @return a certificate that is signed by itself.
     * @throws GeneralSecurityException if the certificate cannot be signed.
     */
    static X509Certificate create(String name, KeyPair keys) throws GeneralSecurityException {
        Instant now = Instant.now();
        byte[] algorithm = sequence(oid(OID_SHA256_WITH_RSA), nullValue());
        byte[] subject = sequence(set(sequence(oid(OID_COMMON_NAME), utf8(name))));

        byte[] tbs = sequence(
                explicit(0, integer(BigInteger.valueOf(2))),                       // version 3
                integer(new BigInteger(63, new SecureRandom()).add(BigInteger.ONE)),
                algorithm,
                subject,                                                            // issuer: self-signed
                sequence(time(now.minus(1, ChronoUnit.DAYS)), time(now.plus(VALID_DAYS, ChronoUnit.DAYS))),
                subject,
                keys.getPublic().getEncoded(),                                      // already a SubjectPublicKeyInfo
                explicit(3, sequence(
                        extension(OID_BASIC_CONSTRAINTS, true, sequence()),         // not a certificate authority
                        extension(OID_EXTENDED_KEY_USAGE, false, sequence(oid(OID_SERVER_AUTH), oid(OID_CLIENT_AUTH))),
                        extension(OID_SUBJECT_ALT_NAME, false, alternativeNames(name))
                )));

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keys.getPrivate());
        signature.update(tbs);

        byte[] encoded = sequence(tbs, algorithm, bitString(signature.sign()));
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(encoded));

        // the JDK parses the structure, and checks the signature with the public key of the certificate.
        certificate.verify(keys.getPublic());
        return certificate;
    }

    private static byte[] alternativeNames(String name) {
        List<byte[]> names = new ArrayList<>();

        if (HOSTNAME.matcher(name).matches()) {
            names.add(context(2, name.getBytes(StandardCharsets.US_ASCII)));
        }
        if (!"localhost".equalsIgnoreCase(name)) {
            names.add(context(2, "localhost".getBytes(StandardCharsets.US_ASCII)));
        }
        for (String address : new String[]{"127.0.0.1", "::1"}) {
            try {
                names.add(context(7, InetAddress.getByName(address).getAddress()));
            } catch (java.net.UnknownHostException e) {
                // literal addresses are not resolved.
                throw new IllegalStateException(e);
            }
        }
        return sequence(names.toArray(new byte[0][]));
    }

    private static byte[] extension(String oid, boolean critical, byte[] value) {
        return critical ?
                sequence(oid(oid), bool(true), octets(value)) :
                sequence(oid(oid), octets(value));
    }

    // ASN.1 DER encoding.

    private static byte[] sequence(byte[]... parts) {
        return encode(0x30, concat(parts));
    }

    private static byte[] set(byte[]... parts) {
        return encode(0x31, concat(parts));
    }

    private static byte[] explicit(int tag, byte[] content) {
        return encode(0xA0 | tag, content);
    }

    private static byte[] context(int tag, byte[] content) {
        return encode(0x80 | tag, content);
    }

    private static byte[] integer(BigInteger value) {
        return encode(0x02, value.toByteArray());
    }

    private static byte[] bool(boolean value) {
        return encode(0x01, new byte[]{(byte) (value ? 0xFF : 0x00)});
    }

    private static byte[] nullValue() {
        return encode(0x05, new byte[0]);
    }

    private static byte[] utf8(String value) {
        return encode(0x0C, value.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] octets(byte[] value) {
        return encode(0x04, value);
    }

    private static byte[] bitString(byte[] value) {
        byte[] content = new byte[value.length + 1];
        // no unused bits in the last byte.
        System.arraycopy(value, 0, content, 1, value.length);
        return encode(0x03, content);
    }

    private static byte[] time(Instant instant) {
        ZonedDateTime utc = instant.atZone(ZoneOffset.UTC);
        // dates up until 2049 are UTCTime, later dates GeneralizedTime (RFC 5280).
        return (utc.getYear() < 2050) ?
                encode(0x17, utc.format(UTC_TIME).getBytes(StandardCharsets.US_ASCII)) :
                encode(0x18, utc.format(GENERALIZED_TIME).getBytes(StandardCharsets.US_ASCII));
    }

    private static byte[] oid(String dotted) {
        String[] arcs = dotted.split("\\.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // the first two arcs are combined into one value.
        base128(out, Long.parseLong(arcs[0]) * 40 + Long.parseLong(arcs[1]));
        for (int i = 2; i < arcs.length; i++) {
            base128(out, Long.parseLong(arcs[i]));
        }
        return encode(0x06, out.toByteArray());
    }

    private static void base128(ByteArrayOutputStream out, long value) {
        int groups = Math.max(1, (64 - Long.numberOfLeadingZeros(value) + 6) / 7);
        for (int i = groups - 1; i >= 0; i--) {
            int group = (int) ((value >> (7 * i)) & 0x7F);
            out.write((i > 0) ? (group | 0x80) : group);
        }
    }

    private static byte[] encode(int tag, byte[] content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(tag);

        if (content.length < 128) {
            out.write(content.length);
        } else {
            int bytes = (32 - Integer.numberOfLeadingZeros(content.length) + 7) / 8;
            out.write(0x80 | bytes);
            for (int i = bytes - 1; i >= 0; i--) {
                out.write((content.length >> (8 * i)) & 0xFF);
            }
        }
        out.write(content, 0, content.length);
        return out.toByteArray();
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.write(part, 0, part.length);
        }
        return out.toByteArray();
    }
}
