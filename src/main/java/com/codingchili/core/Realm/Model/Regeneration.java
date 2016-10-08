package com.codingchili.core.Realm.Model;

import java.io.Serializable;

/**
 * @author Robin Duda
 *         Defines regeneration for a characters main attributes.
 */
class Regeneration implements Serializable {
    private Double health;
    private Double energy;

    public Double getHealth() {
        return health;
    }

    public void setHealth(Double health) {
        this.health = health;
    }

    public Double getEnergy() {
        return energy;
    }

    public void setEnergy(Double energy) {
        this.energy = energy;
    }
}
