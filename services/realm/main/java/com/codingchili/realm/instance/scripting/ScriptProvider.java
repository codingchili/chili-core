package com.codingchili.realm.instance.scripting;

/**
 * @author Robin Duda
 * <p>
 * .
 */
public interface ScriptProvider {

    <T> T apply(Bindings bindings);

    String getEngine();

    String getSource();

}
