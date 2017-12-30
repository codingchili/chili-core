package com.codingchili.realm.instance.scripting;

import com.esotericsoftware.reflectasm.ConstructorAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.logging.Level;

/**
 * @author Robin Duda
 *
 * A 'script' implementation that is written in java.
 */
public class NativeScript implements Scripted {
    public static final String NAME = "java";
    private static Map<String, Function<Bindings, ?>> scripts = new HashMap<>();
    private String className;

    /**
     * For native scripts the source is the class to execute.
     * @param className the name of the class to call.
     */
    public NativeScript(String className) {
        this.className = className;
        scripts.computeIfAbsent(className, this::loadScript);
    }

    @SuppressWarnings("unchecked")
    private Function<Bindings, ?> loadScript(String className) {
        try {
            Class<Function<Bindings, ?>> theClass = (Class<Function<Bindings, ?>>) Class.forName(className);
            ConstructorAccess<Function<Bindings, ?>> access = ConstructorAccess.get(theClass);
            return access.newInstance();
        } catch (ClassNotFoundException e) {
            throw new CoreRuntimeException(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T apply(Bindings bindings) {
        Function<Bindings, ?> script = scripts.get(className);

        if (script == null) {
            bindings.getContext().getLogger(getClass()).event("scriptFailure")
                    .level(Level.ERROR)
                    .send("Failed to load '" + className + "'.");
            return null;
        } else {
            return (T) script.apply(bindings);
        }
    }

    @Override
    public String getEngine() {
        return NAME;
    }

    @Override
    public String getSource() {
        return className;
    }
}
