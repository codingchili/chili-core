package com.codingchili.realm.instance.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Robin Duda
 */
public interface Affliction extends Serializable {

    String getName();

    String getDescription();

    Double getDuration();

    Double getRate();

    Double getChance();
}
