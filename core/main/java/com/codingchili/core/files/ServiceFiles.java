package com.codingchili.core.files;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * The configuration files of a deployable, derived from its class name (the configuration file is named as the
 * service): a deployable in the package {@code <...>.<name>} (for example {@code com.example.realm.Service}, name
 * {@code realm}) reads, relative to the configuration directory:
 * <ul>
 *     <li>{@code service/<name>.<ext>}: its settings ({@link com.codingchili.core.configuration.CoreStrings#getService(String)}),</li>
 *     <li>{@code service/<name>/**}: the files of its folder, if it has one,</li>
 *     <li>{@code <name>/**}: a configuration folder of its own name, for example {@code realm/} with a file per realm.</li>
 * </ul>
 * {@code bake} writes the files of each block from it (an image per block carries only these), {@code generate
 * --encrypt} encrypts the secrets of a file to the nodes whose block reads it, and {@link ConfigurationGuard} warns
 * about a file with secrets that a service loads outside of this set.
 */
public final class ServiceFiles {
    // the folder of the service settings relative to the configuration directory: service/.
    private static final String SERVICES = DIR_SERVICES.substring(DIR_CONFIG.length());

    private ServiceFiles() {
    }

    /**
     * @param deployable the class name of a deployable, for example {@code com.example.realm.Service}.
     * @return the name of its configuration: the last segment of its package ({@code realm}), the class name in
     * lower case without a package.
     */
    public static String name(String deployable) {
        int dot = deployable.lastIndexOf('.');
        if (dot < 0) {
            return deployable.toLowerCase(Locale.ROOT);
        }
        String pkg = deployable.substring(0, dot);
        return pkg.substring(pkg.lastIndexOf('.') + 1);
    }

    /**
     * @param deployable the class name of a deployable.
     * @return the package of the deployable with a trailing dot, its classes are the service's own code.
     */
    public static String pkg(String deployable) {
        int dot = deployable.lastIndexOf('.');
        return (dot < 0) ? "" : deployable.substring(0, dot + 1);
    }

    /**
     * @param deployable the class name of a deployable.
     * @return the files it reads, for people: {@code conf/service/<name>.yaml, conf/service/<name>/, conf/<name>/}.
     */
    public static List<String> describe(String deployable) {
        String name = name(deployable);
        return List.of(DIR_SERVICES + name + EXT_YAML, DIR_SERVICES + name + DIR_SEPARATOR,
                DIR_CONFIG + name + DIR_SEPARATOR);
    }

    /**
     * @param deployable the class name of a deployable.
     * @return the path patterns (regular expressions) of the files it reads: relative to the configuration directory,
     * with forward slashes and without the extension.
     */
    public static List<String> patterns(String deployable) {
        String name = Pattern.quote(name(deployable));
        return List.of(SERVICES + name, SERVICES + name + "/.+", name + "/.+");
    }

    /**
     * @param deployables the class names of deployables, for example the services of a block.
     * @param relative    a configuration file relative to the configuration directory, with forward slashes and an
     *                    extension of {@link ConfigurationFactory#supported()}.
     * @return true when one of the deployables reads the file.
     */
    public static boolean reads(Collection<String> deployables, String relative) {
        return deployables.stream()
                .flatMap(deployable -> patterns(deployable).stream())
                .anyMatch(pattern -> matches(pattern, relative));
    }

    private static boolean matches(String pattern, String relative) {
        String extensions = ConfigurationFactory.supported().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Pattern.compile(pattern + "(" + extensions + ")").matcher(relative).matches();
    }
}
