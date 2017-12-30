package com.codingchili.realm.instance.scripting;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.AbstractMap;

import com.codingchili.core.files.Resource;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * Interface implemented by scripting engines to provide scripting
 * support to object, entities spells and more.
 *
 * Uses a custom serializer to simplify the serialized format.
 *
 * Example:
 * {"scriptedBehavior": {"jexl": "some jexl script;"} }
 */
@JsonDeserialize(using = ScriptedSerializer.class)
public interface Scripted {

    /**
     * @param bindings for the script execution context.
     * @param <T>      type of the scripts return value.
     * @return the return value of the script.
     */
    <T> T apply(Bindings bindings);

    /**
     * @return the name of the scripting engine that the source
     * of the script is written in.
     */
    String getEngine();

    /**
     * @return the scripts source as a text string, this is the serialized
     * form of the script.
     */
    String getSource();




    // todo: remove testing method.
    public static void main(String[] args) {
        Resource res = new Resource("/scripted.yaml");

        System.out.println(res.read().get().toString());

        Scripted s = Serializer.unyaml(res.read().get().toString(), Scripted.class);
        System.out.println(Serializer.yaml(s));

        AbstractMap.Entry<String, String> mapping = new AbstractMap.SimpleEntry<>("jexl", "return 1;");
        String yaml = Serializer.yaml(mapping);
        Scripted sx = Serializer.unyaml(yaml, Scripted.class);

        System.out.println(sx.<Integer>apply(new Bindings()));
    }
}
