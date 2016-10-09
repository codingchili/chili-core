package com.codingchili.core.Realm.Instance.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;

/**
 * @author Robin Duda
 */
public abstract class Attributes {
    protected HashMap attributes = new HashMap<>();
    @JsonIgnore
    protected HashMap<String, Integer> intAttributes = new HashMap<>();
    @JsonIgnore
    protected HashMap<String, Double> doubleAttributes = new HashMap<>();
    @JsonIgnore
    protected HashMap<String, String> stringAttributes = new HashMap<>();

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;

        for (String key : attributes.keySet()) {
            Object object = attributes.get(key);

            if (object instanceof String) {
                stringAttributes.put(key, (String) object);
            } else if (object instanceof Double) {
                doubleAttributes.put(key, (Double) object);
            } else if (object instanceof Integer) {
                intAttributes.put(key, (Integer) object);
            }
        }
    }

    public HashMap getAttributes() {
        return attributes;
    }

    public int getInt(String attribute) {
        return intAttributes.get(attribute);
    }

    public String getString(String attribute) {
        return stringAttributes.get(attribute);
    }

    public Double getDouble(String attribute) {
        return doubleAttributes.get(attribute);
    }

    public Object getObject(String attribute) {
        return attributes.get(attribute);
    }
}
