package com.codingchili.realm.instance.model;

public class Stats {
    public static Stats EMPTY = new Stats();
    private int level;
    private int experience;
    private int health; // armor
    private int movement; // boots, necks, rings?

    // some weapons/armors can have penalties. (heavy armor etc
    // heavyv armor is bad for spellcasting)

    // max hp + hp regen
    private int constitution;
    // cdr (attack speed) + dodge
    private int dexterity;
    // attack power, weapon gloves etc.
    private int strength;
    // spell power
    private int intelligence;
    // not a mana pool.
    private int wisdom;
    // 0-70% reduces magical damage taken.
    private int magicResist;
    // 0-70% reduces physical damage dagen
    private int armorClass;
}
