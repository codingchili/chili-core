package com.codingchili.realm.instance.model.spells;

import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * Implements scripted behavior created by the #{@link ScriptEngine#jexl(String)}.
 */
public interface Scripted<T> {

    /**
     * @param bindings executes the script with the given bindings.
     * @return the result of oscript execution.
     */
    T eval(Map<String, Object> bindings);

    /**
     * @return the source of the script as a string.
     */
    String getSource();

}
