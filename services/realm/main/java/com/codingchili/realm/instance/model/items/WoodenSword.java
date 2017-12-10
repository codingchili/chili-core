package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.stats.Attribute;
import org.apache.commons.jexl2.*;

import com.codingchili.core.protocol.Serializer;

public class WoodenSword extends Item {
    {
        slot = Slot.weapon;
        name = "wooden DAGGr";
        description = "watch out for splinters.";
        usable = true;

        stats.add(Attribute.strength, 4);
        stats.add(Attribute.attackpower, 15);
        stats.add(Attribute.attackspeed, 2);

        recipe.add("wood", 5);
        recipe.add("iron dagger", 1);
        recipe.tool("wooden DAGGr");

        /*onHit = "afflictions.add(source, 'haste')";
        onDamaged = "afflictions.add(target, 'poison')";*/

        modifiers.set(Attribute.health, 1.1f);
    }

    public static void main(String[] args) {
        String yaml = Serializer.yaml(new WoodenSword());
        Item item = Serializer.unyaml(yaml, Item.class);
        String yaml2 = Serializer.yaml(item);
        System.out.println(yaml2);


        JexlEngine engine = new JexlEngine();
        Script expression = engine.createScript("" +
                "stats[attribute.strength];" +
                "stats[attribute.attackpower];" +
                "return 500;");

        JexlContext context = new MapContext();
        context.set("stats", item.stats);
        context.set("attribute", Attribute.class);
        System.out.println(expression.execute(context));

    }
}
