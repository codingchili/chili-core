package com.codingchili.core.testing;

import com.codingchili.core.storage.NestedStorable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Duda
 * <p>
 * Object used for testing, nested within StorageObject.
 */
public class NestedObject implements NestedStorable {
    private String name;
    private List<Integer> numbers = new ArrayList<>();

    public NestedObject() {
        numbers.add(0);
        numbers.add(7);
        numbers.add(42);
    }

    public NestedObject(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public NestedObject setName(String name) {
        this.name = name;
        return this;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public NestedObject setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
        return this;
    }

    @Override
    public int compareTo(Object o) {
        return name.compareTo(((NestedObject) o).getName());
    }
}
