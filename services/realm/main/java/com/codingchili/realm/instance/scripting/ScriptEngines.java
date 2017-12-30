package com.codingchili.realm.instance.scripting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * Encapsulates the scripting implementation.
 */
public class ScriptEngines {
    private static Map<String, Function<String, Scripted>> engines = new HashMap<>();

    static {
        engines.put(JexlScript.NAME, JexlScript::new);
        engines.put(JavaScript.NAME, JavaScript::new);
        engines.put(NativeScript.NAME, NativeScript::new);
    }

    /**
     * Adds a new script engine with the given name.
     *
     * @param engine   the name of the script engine.
     * @param compiler a compiler that takes the source as a string.
     * @return fluent.
     */
    public ScriptEngines add(String engine, Function<String, Scripted> compiler) {
        engines.put(engine, compiler);
        return this;
    }

    /**
     * Creates a new executable script from source.
     *
     * @param source the source to create the script from.
     * @param engine the name of the engine to execute the source.
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
