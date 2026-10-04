package com.codingchili.core.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SLF4J provider that routes logging from dependencies to the {@link ConsoleLogger},
 * registered in META-INF/services. See {@link Slf4jLogger}.
 */
public class Slf4jServiceProvider implements SLF4JServiceProvider {
    private static final String REQUESTED_API_VERSION = "2.0.99";
    private final Map<String, Slf4jLogger> loggers = new ConcurrentHashMap<>();
    private final ILoggerFactory loggerFactory = name -> loggers.computeIfAbsent(name, Slf4jLogger::new);
    private final IMarkerFactory markerFactory = new BasicMarkerFactory();
    private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() {
    }
}
