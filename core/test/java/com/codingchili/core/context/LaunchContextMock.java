package com.codingchili.core.context;

import com.codingchili.core.configuration.Environment;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.testing.LoggerMock;
import com.codingchili.core.testing.MockLogListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Mock of a launcher context.
 */
class LaunchContextMock extends LaunchContext {
    static final String HOST_1 = "host1";
    static final String HOST_2 = "host2";
    static final String HOST_3 = "host3";
    static final String BLOCK_1 = "block1";
    static final String BLOCK_2 = "block2";
    static final String BLOCK_EMPTY = "block_empty";
    static final String BLOCK_NULL = "block-null";
    static final String BLOCK_DEFAULT = "default";
    static final String SERVICE_1 = "service_1";
    static final String SERVICE_2 = "service_2";
    private LoggerMock logger;

    LaunchContextMock(String[] args) {
        super(args);
    }

    public LaunchContextMock(MockLogListener listener) {
        super(new String[]{});
        this.logger = new LoggerMock(listener);
    }

    @Override
    public LauncherSettings settings() {
        HashMap<String, List<String>> blocks = new HashMap<>();
        HashMap<String, String> hosts = new HashMap<>();
        LauncherSettings settings = new LauncherSettings();

        hosts.put(HOST_1, BLOCK_1);
        hosts.put(HOST_2, BLOCK_1);
        hosts.put(HOST_3, BLOCK_NULL);

        if (Environment.hostname().isPresent()) {
            hosts.put(Environment.hostname().get(), BLOCK_2);
        }

        if (Environment.addresses().size() > 0) {
            hosts.put(Environment.addresses().get(0), BLOCK_2);
        }

        List<String> services1 = new ArrayList<>();
        List<String> services2 = new ArrayList<>();
        services1.add(SERVICE_1);
        services2.add(SERVICE_2);

        blocks.put(BLOCK_1, services1);
        blocks.put(BLOCK_2, services2);
        blocks.put(BLOCK_EMPTY, new ArrayList<>());
        blocks.put(BLOCK_DEFAULT, new ArrayList<>());

        settings.setHosts(hosts);
        settings.setBlocks(blocks);

        return settings;
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
