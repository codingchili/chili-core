package com.codingchili.core.storage;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Must be implemented by objects that are nested into Storables.
 * Removes the need for implementing id() method.
 */
public interface NestedStorable extends Storable {

    @JsonIgnore
    default String getId() {
        return "";
    }

}
