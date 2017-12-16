package com.codingchili.realm.instance.model.spells;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.*;

import com.codingchili.core.files.Resource;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 */
@JsonDeserialize(using = ScriptedSerializer.class)
public interface Scripted {

    <T> T apply(Bindings bindings);

    String getEngine();

    String getSource();

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
