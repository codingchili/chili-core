package com.codingchili.core.files;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.logging.ConsoleLogger;
import com.codingchili.core.logging.Level;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.AuthenticationGenerator;
import com.codingchili.core.security.SecretRecipients;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * The safety net of the images per block: a block image carries only the configuration files named as its services
 * ({@link ServiceFiles}). {@link Configurations} records each file it loads and the classes that loaded it; once
 * the launcher deployed its services ({@link #deployed(Collection)}), and on every later load, a file with generated
 * secrets (security dependencies) that none of the services of this JVM owns is reported, naming the service that
 * loaded it (the outermost class on the stack in the package of a deployed service), the file and the fix. A warning
 * in development; with {@link com.codingchili.core.configuration.system.SystemSettings#strict()} (production) the
 * start fails instead, and a later load is logged as an error.
 */
public final class ConfigurationGuard {
    private static final Set<String> FRAMEWORK = Set.of("java.", "javax.", "jdk.", "sun.", "io.vertx.", "io.netty.",
            "com.hazelcast.", "tools.jackson.", "com.fasterxml.", "com.codingchili.core.");
    private static final Map<String, Loaded> loaded = new ConcurrentHashMap<>();
    private static final Logger logger = new ConsoleLogger(ConfigurationGuard.class);
    // the deployables of this JVM once the launcher deployed them, null before.
    private static volatile List<String> deployed;

    private ConfigurationGuard() {
    }

    /**
     * Records a loaded configuration file, see the class.
     *
     * @param path   the file as loaded.
     * @param fields the keys of the file.
     */
    static void loaded(String path, Set<String> fields) {
        var entry = new Loaded(Set.copyOf(fields), loaders());
        loaded.put(path, entry);
        List<String> services = deployed;
        if (services != null) {
            check(path, entry, services).ifPresent(violation -> {
                if (Configurations.system().strict()) {
                    logger.log(violation, Level.ERROR);
                } else {
                    logger.log(violation, Level.WARNING);
                }
            });
        }
    }

    /**
     * Checks the files loaded so far, called by the launcher once the services of its block are deployed; later
     * loads are checked as they happen.
     *
     * @param deployables the class names of the services deployed in this JVM.
     * @return the reports, one per file, empty when every file with secrets belongs to a deployed service.
     * @throws CoreRuntimeException in strict mode when there is a report.
     */
    public static List<String> deployed(Collection<String> deployables) {
        List<String> services = List.copyOf(deployables);
        deployed = services;
        List<String> violations = new ArrayList<>();
        new TreeMap<>(loaded).forEach((path, entry) -> check(path, entry, services).ifPresent(violations::add));

        if (!violations.isEmpty()) {
            if (Configurations.system().strict()) {
                throw new CoreRuntimeException(getConfigurationNotOwnedStrict(violations));
            }
            violations.forEach(violation -> logger.log(violation, Level.WARNING));
        }
        return violations;
    }

    /**
     * Forgets the loaded files and the deployed services, for tests.
     */
    public static void reset() {
        loaded.clear();
        deployed = null;
    }

    private static Optional<String> check(String path, Loaded entry, List<String> services) {
        String directory = Configurations.launcher().getConfigurationDirectory();
        String relative = SecretRecipients.relative(directory, path);
        if (relative.startsWith("..") || ServiceFiles.reads(services, relative)) {
            return Optional.empty();
        }
        Set<String> keys = new TreeSet<>(AuthenticationGenerator.generatedKeys(path));
        keys.retainAll(entry.fields);
        if (keys.isEmpty()) {
            return Optional.empty();
        }
        List<String> files = services.stream().flatMap(service -> ServiceFiles.describe(service).stream()).toList();
        return Optional.of(getConfigurationNotOwned(path, keys, loader(entry, services), services, files));
    }

    // the service that loaded the file: the outermost class of the stack in the package of a deployed service.
    private static String loader(Loaded entry, List<String> services) {
        for (int i = entry.loaders.size() - 1; i >= 0; i--) {
            String loader = entry.loaders.get(i);
            for (String service : services) {
                String pkg = ServiceFiles.pkg(service);
                if (!pkg.isEmpty() && loader.startsWith(pkg)) {
                    return service + " (" + loader + ")";
                }
            }
        }
        return entry.loaders.isEmpty() ? "an unknown class" : entry.loaders.get(entry.loaders.size() - 1);
    }

    // the classes on the stack outside of the framework, innermost first.
    private static List<String> loaders() {
        return StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(name -> FRAMEWORK.stream().noneMatch(name::startsWith))
                .distinct()
                .limit(64)
                .toList());
    }

    private record Loaded(Set<String> fields, List<String> loaders) {
    }
}
