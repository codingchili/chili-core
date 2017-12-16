package com.codingchili.realm.instance.model.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Encapsulates the scripting implementation.
 */
public class ScriptEngine {
    private static Map<String, Function<String, Scripted>> engines = new HashMap<>();

    static {
        engines.put(JexlScript.NAME, JexlScript::new);
        engines.put(NativeScript.NAME, NativeScript::new);
    }

    /**
     * Creates a new executable script from source.
     *
     * @param source the source to create the script from.
     * @return an executable script.
     */
    public static Scripted script(String source, String engine) {
        Function<String, Scripted> provider = engines.get(engine);

        if (provider != null) {
            return provider.apply(source);
        } else {
            throw new CoreRuntimeException(
                    String.format("No script engine registered with name '%s'.", engine));
        }
    }
}
