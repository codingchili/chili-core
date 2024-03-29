package com.codingchili.core.testing;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.storage.AttributeRegistration;
import com.codingchili.core.storage.AttributeRegistry;
import com.codingchili.core.storage.Storable;

/**
 * Test class to simulate a stored object.
 */
public class StorageObject implements Storable {
    public static final String NESTED_PREFIX = "nested.";
    public static String levelField = "level";
    public String name;
    public Integer level;
    public NestedObject nested;
    public List<String> keywords = new ArrayList<>();

    static {
        AttributeRegistry.register(db -> {
            db.single(StorageObject::getId, String.class, "id");
            db.single(StorageObject::getName, String.class, "name");
            db.single(StorageObject::getLevel, Integer.class, "level");
            db.multi(StorageObject::getKeywords, String.class, "keywords[]");
            db.single(s -> s.nested.getName(), String.class, "nested.name");
            db.multi(s -> s.nested.getNumbers(), Integer.class, "nested.numbers[]");
        }, StorageObject.class);
    }

    public StorageObject() {
        keywords.add("keyword");
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

    public StorageObject setLevel(Integer level) {
        this.level = level;
        return this;
    }

    @Override
    public String getId() {
        return name;
    }

    public String getName() {
        return name;
    }

    public StorageObject setName(String name) {
        this.name = name;
        return this;
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
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(other) == 0;
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
