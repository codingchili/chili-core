package com.codingchili.core.configuration;

import java.net.*;
import java.util.*;

/**
 * @author Robin Duda
 *         <p>
 *         Utility class to get environment information. Use with care.
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
}