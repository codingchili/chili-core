package com.codingchili.core.testing;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 *         <p>
 *         Test class to simulate a stored object.
 */
public class StorageObject implements Storable {
    public static final String NESTED_PREFIX = "nested.";
    private String name;
    private Integer level;
    private NestedObject nested;
    private List<String> keywords = new ArrayList<>();

    public StorageObject() {
        keywords.add("Z");
    }

    public StorageObject(String name, Integer level) {
        this();
        this.name = name;
        this.level = level;
        this.nested = new NestedObject(NESTED_PREFIX + name);
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String id() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NestedObject getNested() {
        return nested;
    }

    public void setNested(NestedObject nested) {
        this.nested = nested;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String toString() {
        return "name=" + name + " " + "level=" + level;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StorageObject && (name.equals(((StorageObject) other).name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        return name.compareTo(((StorageObject) o).id());
    }

    @Override
    public int compareToAttribute(Storable other, String attribute) {
        StorageObject item = (StorageObject) other;

        switch (attribute) {
            case "level":
                return level.compareTo(item.getLevel());
            case "nested.name":
                return nested.getName().compareTo(item.getNested().getName());
            default:
                return 0;
        }
    }
}