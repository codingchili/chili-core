package com.codingchili.core.storage;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vertx.core.shareddata.Shareable;

/**
 * @author Robin Duda
 *         <p>
 *         All classes using the storage system must implement storable.
 *         <p>
 *         If not errors such as "Not serializable" or "Illegal argument exception" will occur.
 */
public interface Storable extends Serializable, Cloneable, Comparable, Shareable {
    String idField = "id";

    /**
     * Get the id of the storable item, used as a key to the map api.
     * This field is not included when serializing to JSON. When serializing to json
     * the id must be kept separately.
     * <p>
     * instead of implementing this method, to avoid duplicate values in values
     * as keys; implement hashCode based on an unique attribute combination.
     *
     * @return an id that is unique to this storable item.
     */
    @JsonIgnore
    default String id() {
        return this.hashCode() + "";
    }

    /**
     * Provides a default implementation for compareTo that uses the storables id.
     *
     * @param other the object to compare to.
     * @return see Comparable#compareTo(Object)
     */
    @Override
    default int compareTo(Object other) {
        if (other instanceof Storable) {
            return id().compareTo(((Storable) other).id());
        } else {
            return -1;
        }
    }

    /**
     * compares this storable to another storable, with an attribute specified.
     * this method must be implemented by the storable to allow ordering. Only
     * required for storages utilizing comparators for ordering. The implementation
     * may be based on attribute names mapped to getter lambdas, or a simple switch.
     * <p>
     * Only required when ordering by attribute with {@link HazelMap}
     *
     * @param other     the other object to compare to.
     * @param attribute handler of the attribute that should be tested.
     * @return integer that matches implementation of @see Comparable#compareTo(Object)
     */
    @JsonIgnore
    default int compareToAttribute(Storable other, String attribute) {
        return 0; // natural ordering as default.
    }
}
