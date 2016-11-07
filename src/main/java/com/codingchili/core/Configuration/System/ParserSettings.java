package com.codingchili.core.Configuration.System;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.Configuration.RegexComponent;

/**
 * @author Robin Duda
 */
public class ParserSettings {
    public List<RegexComponent> regex;
    public ArrayList<String> keys;
    public int[] length;

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
}
