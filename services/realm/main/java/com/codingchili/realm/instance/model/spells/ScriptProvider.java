package com.codingchili.realm.instance.model.spells;

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
