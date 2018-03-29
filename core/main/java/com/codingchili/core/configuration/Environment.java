package com.codingchili.core.configuration;

import java.net.*;
import java.util.*;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Utility class to get environment information. Use with care.
 */
public class Environment {

    /**
     * @return the hostname of the current machine if available.
     */
    public static Optional<String> hostname() {
        try {
            return Optional.of(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    /**
     * @return an IP address of this host. Which address that is returned is
     * not deterministic in case of multiple network interfaces. It should however
     * not return the loopback address.
     */
    public static String address() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    /**
     * @return a list of all addresses available on all network interfaces.
     */
    public static List<String> addresses() {
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();

            while (enumeration.hasMoreElements()) {
                NetworkInterface neterface = enumeration.nextElement();

                neterface.getInterfaceAddresses().forEach(address -> {
                    addresses.add(address.getAddress().getHostAddress());
                });
            }
        } catch (SocketException ignore) {
        }
        return addresses;
    }

    /**
     * @return true if running on Java 9.
     */
    public static boolean isJava9() {
        return System.getProperty("java.version").equals("9");
    }
}