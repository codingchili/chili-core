package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.spells.Affliction;

import java.util.HashMap;

import com.codingchili.core.protocol.Serializer;

import static com.codingchili.realm.instance.model.spells.ScriptEngine.script;

/**
 * @author Robin Duda
 */
public class SimpleAffliction extends Affliction {
    {
        name = "Bleeding";
        description = "you are injured.";
        duration = 30;
        tick = script("return 1;");
        modifier = script("return 2;");
    }

    public static void main(String[] args) {
        Affliction aff = new SimpleAffliction();
        String yaml = Serializer.yaml(aff);
        System.out.println(yaml);

        Affliction affYaml = Serializer.unyaml(yaml, Affliction.class);

        System.out.println(affYaml.apply(new HashMap<>()));
    }
}
