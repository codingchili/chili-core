package com.codingchili.core.configuration.system;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.codingchili.core.configuration.RegexComponent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains settings for validating strings with regexes, length and field names.
 */
public class ParserSettings {
    public String name;
    public Set<String> keys = new HashSet<>();
    public List<RegexComponent> regex = new ArrayList<>();
    public int[] length = new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE};

    /**
     * Creates a new settings object with no field validation enabled by default.
     *
     * @param name the name of the ruleset.
     */
    @JsonCreator
    public ParserSettings(@JsonProperty("name") String name) {
        this.name = name;
    }

    /**
     * @return the name of the ruleset.
     */
    public String getName() {
        return name;
    }

    /**
     * @return a list of regexes to apply for the matching fields.
     */
    public List<RegexComponent> getRegex() {
        return regex;
    }

    /**
     * @param regex a list of regular expressions to apply on matching fields.
     * @return fluent.
     */
    public ParserSettings setRegex(List<RegexComponent> regex) {
        this.regex = regex;
        return this;
    }

    /**
     * @return a list of field names for which validation is to be enabled.
     * if empty - all fields are validated.
     */
    public Set<String> getKeys() {
        return keys;
    }

    /**
     * @param keys a list of fields that will be validated, if empty then all
     *             fields are validated.
     */
    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }

    /**
     * @return an array of a minimum and maximum field length.
     */
    public int[] getLength() {
        return length;
    }

    /**
     * @param length the min and max lengths for the validated field.
     */
    public void setLength(int[] length) {
        this.length = length;
    }

    /**
     * @param key specifies that this key needs validation, when called validation for all fields
     *            not listed are disabled. The keys does not consider the object hierarchy, this is
     *            a plain field name.
     * @return fluent.
     */
    @JsonIgnore
    public ParserSettings addKey(String key) {
        keys.add(key);
        return this;
    }

    /**
     * @param keys a list of keys to add, see {@link #addKey(String)}.
     * @return fluent.
     */
    public ParserSettings addKeys(String... keys) {
        for (String key: keys) {
            addKey(key);
        }
        return this;
    }

    /**
     * @param min the minimum length of the field being validated.
     * @param max the maximum length of the field being validated.
     * @return fluent.
     */
    @JsonIgnore
    public ParserSettings length(int min, int max) {
        length[0] = min;
        length[1] = max;
        return this;
    }

    /**
     * @param component the regular expression configuration used to match input.
     * @return fluent.
     */
    @JsonIgnore
    public ParserSettings addRegex(RegexComponent component) {
        regex.add(component);
        return this;
    }

    /**
     * @param field the field to check if validation should be performed.
     * @return true if the field needs validation, if it's added to the checked
     * keys or if the fieldset is empty.
     */
    @JsonIgnore
    public boolean isFieldValidated(String field) {
        return keys.isEmpty() || keys.contains(field);
    }
}
