package com.codingchili.realm.instance.model;

import java.util.*;

/**
 * @author Robin Duda
 */
public interface Interactable {

    default Set<Interactions> getInteractions() {
        return Collections.emptySet();
    }

    enum Interactions {INSPECT, TRADE, DIALOG, FRIEND}
}
