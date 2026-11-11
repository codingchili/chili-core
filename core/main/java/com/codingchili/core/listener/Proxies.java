package com.codingchili.core.listener;

import io.netty.util.NetUtil;

import java.util.*;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreRuntimeException;

/**
 * The proxies that a listener trusts to tell it the address of the client, with the
 * X-Forwarded-For header ({@link CoreStrings#PROTOCOL_FORWARDED_FOR}), see {@link ListenerSettings#getProxies()}.
 * <p>
 * The header is only read when the peer of the connection is a trusted proxy. Each proxy appends the address
 * it received the request from, so the header is read from the right: the client is the rightmost address
 * that is not a trusted proxy. The addresses to the left of it were written by the client and are ignored.
 * When the peer is not trusted, the header is ignored and the peer is the client.
 * <p>
 * Addresses are IP literals only: nothing is resolved with DNS.
 */
public class Proxies {
    private static final String CIDR = "/";
    private static final String HOPS = ",";
    private final List<Range> ranges = new ArrayList<>();

    /**
     * @param proxies the trusted proxies: IP addresses (10.0.0.1, ::1) or CIDR ranges (10.0.0.0/8, fd00::/8).
     *                Empty trusts no proxy.
     * @throws CoreRuntimeException when an entry is not an IP address or a CIDR range.
     */
    public Proxies(Collection<String> proxies) {
        if (proxies != null) {
            for (String proxy : proxies) {
                ranges.add(Range.parse(proxy));
            }
        }
    }

    /**
     * @return true if no proxy is trusted: the address of the client is always the peer.
     */
    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    /**
     * @param address an IP address.
     * @return true if the address is a trusted proxy, false if it is not or is not an IP address.
     */
    public boolean trusted(String address) {
        var bytes = parse(address);
        return bytes != null && trusted(bytes);
    }

    /**
     * Resolves the address of the client of a request.
     *
     * @param peer      the address of the peer of the connection.
     * @param forwarded the values of the X-Forwarded-For headers of the request, in order, may be empty.
     * @return the peer when it is not a trusted proxy, otherwise the rightmost forwarded address that is
     * not a trusted proxy. When every forwarded address is trusted, the leftmost; when an address is not an IP
     * address, the last trusted hop before it (to the right).
     */
    public String client(String peer, List<String> forwarded) {
        if (ranges.isEmpty() || forwarded == null || forwarded.isEmpty() || !trusted(peer)) {
            return peer;
        }
        var client = peer;
        for (int i = forwarded.size() - 1; i >= 0; i--) {
            var hops = forwarded.get(i).split(HOPS);
            for (int j = hops.length - 1; j >= 0; j--) {
                var hop = hops[j].trim();
                if (hop.isEmpty()) {
                    continue;
                }
                var bytes = parse(stripPort(hop));
                if (bytes == null) {
                    return client;
                }
                client = NetUtil.bytesToIpAddress(bytes);
                if (!trusted(bytes)) {
                    return client;
                }
            }
        }
        return client;
    }

    private boolean trusted(byte[] address) {
        for (Range range : ranges) {
            if (range.contains(address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the port of [::1]:80 and 10.0.0.1:80, proxies normally don't add one.
     */
    private static String stripPort(String hop) {
        if (hop.startsWith("[")) {
            var end = hop.indexOf(']');
            return (end > 0) ? hop.substring(1, end) : hop;
        }
        var colon = hop.indexOf(':');
        if (colon > 0 && colon == hop.lastIndexOf(':')) {
            return hop.substring(0, colon);
        }
        return hop;
    }

    /**
     * @return the bytes of an IP literal, IPv4-mapped IPv6 addresses as IPv4; null if not an IP literal.
     */
    private static byte[] parse(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        var bytes = NetUtil.createByteArrayFromIpAddressString(address);
        if (bytes != null && bytes.length == 16 && isMappedIpv4(bytes)) {
            bytes = Arrays.copyOfRange(bytes, 12, 16);
        }
        return bytes;
    }

    private static boolean isMappedIpv4(byte[] bytes) {
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff;
    }

    private record Range(byte[] network, int prefix) {

        static Range parse(String proxy) {
            if (proxy == null) {
                throw new CoreRuntimeException(CoreStrings.getInvalidProxy(null));
            }
            var value = proxy.trim();
            var slash = value.indexOf(CIDR);
            var bytes = Proxies.parse((slash < 0) ? value : value.substring(0, slash));
            if (bytes == null) {
                throw new CoreRuntimeException(CoreStrings.getInvalidProxy(proxy));
            }
            var bits = bytes.length * 8;
            var prefix = bits;
            if (slash >= 0) {
                try {
                    prefix = Integer.parseInt(value.substring(slash + 1));
                } catch (NumberFormatException e) {
                    throw new CoreRuntimeException(CoreStrings.getInvalidProxy(proxy));
                }
                if (bytes.length == 4 && isMappedRange(value, slash)) {
                    // ::ffff:10.0.0.0/104 is 10.0.0.0/8.
                    prefix -= 96;
                }
                if (prefix < 0 || prefix > bits) {
                    throw new CoreRuntimeException(CoreStrings.getInvalidProxy(proxy));
                }
            }
            return new Range(bytes, prefix);
        }

        private static boolean isMappedRange(String value, int slash) {
            return value.substring(0, slash).contains(":");
        }

        boolean contains(byte[] address) {
            if (address.length != network.length) {
                return false;
            }
            var full = prefix / 8;
            for (int i = 0; i < full; i++) {
                if (address[i] != network[i]) {
                    return false;
                }
            }
            var rest = prefix % 8;
            if (rest == 0) {
                return true;
            }
            var mask = (0xff << (8 - rest)) & 0xff;
            return (address[full] & mask) == (network[full] & mask);
        }
    }
}
