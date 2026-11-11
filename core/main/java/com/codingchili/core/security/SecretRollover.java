package com.codingchili.core.security;

import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.codingchili.core.configuration.CoreStrings.SECRET_PREVIOUS_SUFFIX;
import static io.vertx.core.json.impl.JsonUtil.BASE64_DECODER;

/**
 * Secret rollover: a secret key in a configuration file may have a previous value next to it, under the same
 * key with the suffix {@value com.codingchili.core.configuration.CoreStrings#SECRET_PREVIOUS_SUFFIX}
 * (for example clientSecret and clientSecretPrevious), written by the generator when it rotates the secrets.
 * <p>
 * Configuration files are registered here when they are loaded (and reloaded on change). A {@link TokenFactory}
 * created with any value of a registered key, current or earlier, signs with the current value and accepts tokens
 * that verify with the current or the previous value. This lets a running node pick up rotated secrets without a
 * restart, even where it keeps a token factory created with the secret of its startup, and keeps tokens signed
 * with the previous secret valid until the previous value is removed (retired) or they expire.
 */
public final class SecretRollover {
    // path#key: the current and previous secret of a key in a configuration file.
    private static final Map<String, Rollover> entries = new ConcurrentHashMap<>();
    // every secret seen as the current or previous value of an entry, to the entry.
    private static final Map<String, String> history = new ConcurrentHashMap<>();

    private SecretRollover() {
    }

    /**
     * Registers the secret keys of a configuration file: the keys with a previous value, and keys that were
     * registered before (a retired previous value is removed).
     *
     * @param path   the path of the configuration file.
     * @param config the contents of the file.
     */
    public static void register(String path, JsonObject config) {
        String prefix = path + "#";
        Set<String> keys = new HashSet<>();

        entries.keySet().stream()
                .filter(id -> id.startsWith(prefix))
                .forEach(id -> keys.add(id.substring(prefix.length())));

        config.fieldNames().stream()
                .filter(key -> key.endsWith(SECRET_PREVIOUS_SUFFIX) && key.length() > SECRET_PREVIOUS_SUFFIX.length())
                .forEach(key -> keys.add(key.substring(0, key.length() - SECRET_PREVIOUS_SUFFIX.length())));

        for (String key : keys) {
            String id = prefix + key;
            byte[] current = decode(config.getValue(key));

            if (current == null) {
                entries.remove(id);
            } else {
                byte[] previous = decode(config.getValue(key + SECRET_PREVIOUS_SUFFIX));
                entries.put(id, new Rollover(current, previous));
                history.put(id(current), id);
                if (previous != null) {
                    history.put(id(previous), id);
                }
            }
        }
    }

    /**
     * @param secret a secret that a token factory was created with.
     * @return the secret to sign with: the current value of the key that the secret is or was a value of,
     * otherwise the secret itself.
     */
    static byte[] signing(byte[] secret) {
        return find(secret).map(rollover -> rollover.current).orElse(secret);
    }

    /**
     * @param secret a secret that a token factory was created with.
     * @return the secrets that a token may be signed with: the current and previous value of the key that the
     * secret is or was a value of, otherwise only the secret itself.
     */
    static List<byte[]> verifying(byte[] secret) {
        return find(secret).map(Rollover::accepted).orElse(List.of(secret));
    }

    /**
     * Forgets every registered secret, for tests: {@link com.codingchili.core.files.Configurations#reset()}.
     */
    public static void clear() {
        entries.clear();
        history.clear();
    }

    private static Optional<Rollover> find(byte[] secret) {
        String id = history.get(id(secret));
        return (id == null) ? Optional.empty() : Optional.ofNullable(entries.get(id));
    }

    private static String id(byte[] secret) {
        return Base64.getEncoder().encodeToString(secret);
    }

    private static byte[] decode(Object value) {
        if (value instanceof String text && !text.isBlank()) {
            try {
                return BASE64_DECODER.decode(text);
            } catch (IllegalArgumentException e) {
                try {
                    return Base64.getDecoder().decode(text);
                } catch (IllegalArgumentException ignored) {
                    // not a secret.
                }
            }
        }
        return null;
    }

    private record Rollover(byte[] current, byte[] previous) {
        List<byte[]> accepted() {
            return (previous == null) ? List.of(current) : List.of(current, previous);
        }
    }
}
