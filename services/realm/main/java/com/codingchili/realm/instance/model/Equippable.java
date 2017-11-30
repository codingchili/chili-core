package com.codingchili.realm.instance.model;

import java.util.Map;

/**
 * @author Robin Duda
 */
public interface Equippable {

    String slot();

    Map<String, Integer> stats();
}
