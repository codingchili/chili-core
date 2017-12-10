package com.codingchili.realm.instance.model.spells;

import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.Script;

/**
 * @author Robin Duda
 * <p>
 * Encapsulates the scripting implementation.
 */
public class ScriptEngine {
    private static JexlEngine engine = new JexlEngine();

    /**
     * Creates a new jexl script.
     * @param source the jexl source to create a script from.
     * @return an instance of a jexl script.
     */
    static Script jexl(String source) {
        return engine.createScript(source);
    }

    /**
     * Creates a new executable script from source.
     *
     * @param source the source to create the script from.
     * @return an executable script.
     */
    public static Scripted script(String source) {
        return new JexlScript().setSource(source);
    }
}
