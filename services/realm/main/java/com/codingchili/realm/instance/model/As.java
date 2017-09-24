package com.codingchili.realm.instance.model;

/**
 * @author Robin Duda
 * Defines how a attribute/value modifier is interpreted.
 * <p>
 * max - the max value
 * current - the current value
 * <p>
 * example:
 * <p>
 * modify: health, as [current], value -0.01
 */
public enum As {
    max, current
}
