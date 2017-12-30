package com.codingchili.realm.instance.scripting;

import org.apache.commons.jexl2.*;

/**
 * @author Robin Duda
 * <p>
 * Serializable jexl script.
 */
public class JexlScript implements Scripted {
    public static final String NAME = "jexl";
    private static JexlEngine jexlEngine = new JexlEngine();
    private Script script;

    public JexlScript(String source) {
        this.script = jexlEngine.createScript(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T apply(Bindings bindings) {
        JexlContext context = new MapContext();
        bindings.forEach(context::set);
        return (T) script.execute(context);
    }

    @Override
    public String getEngine() {
        return NAME;
    }

    @Override
    public String getSource() {
        return script.getText();
    }
}
