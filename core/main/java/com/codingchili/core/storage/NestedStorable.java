package com.codingchili.core.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 * <p>
 * Must be implemented by objects that are nested into Storables.
 * Removes the need for implementing id() method.
 */
public interface NestedStorable extends Storable {

    @JsonIgnore
    default String id() {
        return "";
    }

}
