package com.codingchili.core.configuration.exception;

import java.util.Collection;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.context.CoreException;

/**
 * Thrown when the launcher deploys by host (no block given) while hosts are configured, and neither the hostname
 * is a block nor the hostname or an address of the host is in the hosts.
 */
public class HostNotConfiguredException extends CoreException {
    /**
     * @param hostname the hostname of this machine, null if unknown.
     * @param hosts    the configured hosts.
     * @param blocks   the configured blocks.
     */
    public HostNotConfiguredException(String hostname, Collection<String> hosts, Collection<String> blocks) {
        super(CoreStrings.getHostNotConfigured(hostname, hosts, blocks));
    }
}
