package com.codingchili.core.configuration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

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