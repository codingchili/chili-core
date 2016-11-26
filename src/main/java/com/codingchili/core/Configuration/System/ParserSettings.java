package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.configuration.RegexComponent;

/**
 * @author Robin Duda
 *
 * Contains settings for validating strings with regexes, length and field names.
 */
public class ParserSettings {
    public ArrayList<String> keys = new ArrayList<>();
    public List<RegexComponent> regex = new ArrayList<>();
    public int[] length = new int[2];

    public ParserSettings() {}

    public List<RegexComponent> getRegex() {
        return regex;
    }

    public void setRegex(List<RegexComponent> regex) {
        this.regex = regex;
    }

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    public int[] getLength() {
        return length;
    }

    public void setLength(int[] length) {
        this.length = length;
    }

    @JsonIgnore
    ParserSettings addKey(String key) {
        keys.add(key);
        return this;
    }

    @JsonIgnore
    ParserSettings length(int min, int max) {
        length[0] = min;
        length[1] = max;
        return this;
    }

    @JsonIgnore
    ParserSettings addRegex(RegexComponent component) {
        regex.add(component);
        return this;
    }
}
