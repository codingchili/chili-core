package com.codingchili.core.Realm.Model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *
 * base stats for a character and may be used in items too.
 */
class Attributes implements Serializable {
    private int power;
    private int attack;
    private int spell;
    private int defence;
    private int resistance;
    private int stamina;

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getSpell() {
        return spell;
    }

    public void setSpell(int spell) {
        this.spell = spell;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public int getResistance() {
        return resistance;
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }
}
