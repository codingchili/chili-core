package com.codingchili.realm.instance.model.items;

import com.codingchili.realm.instance.model.stats.Attribute;
import org.apache.commons.jexl2.*;

import com.codingchili.core.protocol.Serializer;

public class WoodenSword extends Item {
    {
        slot = Slot.weapon;
        weapon = WeaponType.sword2h;
        name = "wooden DAGGr";
        description = "watch out for splinters.";

        stats.update(Attribute.strength, 4);
        stats.update(Attribute.attackpower, 15);
        stats.update(Attribute.attackspeed, 2);
        stats.set(Attribute.health, 1.1f);

        recipe.add("wood", 5);
        recipe.add("iron dagger", 1);
        recipe.tool("wooden DAGGr");

        /*onHit = "afflictions.add(source, 'haste')";
        onDamaged = "afflictions.add(target, 'poison')";*/

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
