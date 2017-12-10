package com.codingchili.realm.instance.model.spells;

import org.apache.commons.jexl2.*;

import java.util.Map;

/**
 * @author Robin Duda
 * <p>
 * Serializable jexl script.
 */
public class JexlScript implements Scripted<Object> {
    private ThreadLocal<MapContext> local = ThreadLocal.withInitial(MapContext::new);
    private Script script;
    private String source;

    @Override
    public Object eval(Map<String, Object> bindings) {
        JexlContext context = local.get();
        bindings.forEach(context::set);
        return script.execute(context);
    }

    @Override
    public String getSource() {
        return source;
    }

    public JexlScript setSource(String source) {
        this.source = source;
        this.script = ScriptEngine.jexl(source);
        return this;
    }
}
