package com.codingchili.core.configuration;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author Robin Duda
 *         <p>
 *         Extended by classes with dynamic attributes.
 */
public abstract class Attributes {
    protected HashMap<String, Object> attributes = new HashMap<>();

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void put(String key, Object object) {
        attributes.put(key, object);
    }

    public void clear() {
        attributes.clear();
    }

    public int size() {
        return attributes.size();
    }

    public void remove(String key) {
        attributes.remove(key);
    }

    public Optional<Object> getObject(String key) {
        if (attributes.containsKey(key)) {
            return Optional.of(attributes.get(key));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Integer> getInt(String key) {
        if (attributes.get(key) instanceof Integer) {
            return Optional.of((Integer) attributes.get(key));
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getString(String key) {
        if (attributes.get(key) instanceof String) {
            return Optional.of((String) attributes.get(key));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Double> getDouble(String key) {
        if (attributes.get(key) instanceof Double) {
            return Optional.of((Double) attributes.get(key));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Boolean> getBool(String key) {
        if (attributes.get(key) instanceof Boolean) {
            return Optional.of((Boolean) attributes.get(key));
        } else {
            return Optional.empty();
        }
    }
}
