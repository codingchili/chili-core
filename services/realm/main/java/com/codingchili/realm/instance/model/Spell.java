package com.codingchili.realm.instance.model;

import java.util.List;

/**
 * @author Robin Duda
 */
public interface Spell {

    String getName();

    String getDescription();

    Target getTarget();

    Float getCooldown();

    Float getCasttime();

    Boolean isPassive();

    String getCost();

    List<Affliction> getAfflictions();
}
