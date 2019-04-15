package com.codingchili.core.configuration.system;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.codingchili.core.configuration.RegexComponent;
import com.codingchili.core.security.RegexAction;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains settings for validating strings with regexes, length and field names.
 */
public class ValidatorSettings {
    private List<RegexComponent> regex = new ArrayList<>();
    private Set<String> keys = new HashSet<>();
    private int minLength = Integer.MIN_VALUE;
    private int maxLength = Integer.MAX_VALUE;
    private String name;

    /**
     * Creates a new settings object with no field validation enabled by default.
     *
     * @param name the name of the ruleset.
     */
    @JsonCreator
    public ValidatorSettings(@JsonProperty("name") String name) {
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
    public ValidatorSettings setRegex(List<RegexComponent> regex) {
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
     * @return the minimum length of the input.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * @param minLength the minimum length of the input.
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @return the maximum length of the input.
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * @param maxLength the maximum length of the input.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @param key specifies that this key needs validation, when called validation for all fields
     *            not listed are disabled. The keys does not consider the object hierarchy, this is
     *            a plain field name.
     * @return fluent.
     */
    @JsonIgnore
    public ValidatorSettings addKey(String key) {
        keys.add(key);
        return this;
    }

    /**
     * @param keys a list of keys to add, see {@link #addKey(String)}.
     * @return fluent.
     */
    @JsonIgnore
    public ValidatorSettings addKeys(String... keys) {
        for (String key : keys) {
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
    public ValidatorSettings length(int min, int max) {
        minLength = min;
        maxLength = max;
        return this;
    }

    /**
     * @param action     {@link #addRegex(RegexAction, String, String)}
     * @param expression the regular expression configuration used to match input.
     * @return fluent.
     */
    @JsonIgnore
    public ValidatorSettings addRegex(RegexAction action, String expression) {
        return addRegex(action, expression, "");
    }

    /**
     * @param action       the action to take when the expression matches the input.
     * @param expression   the regular expression configuration used to match input.
     * @param substitution used when {@link RegexAction#SUBSTITUTE} is set.
     * @return fluent.
     */
    @JsonIgnore
    public ValidatorSettings addRegex(RegexAction action, String expression, String substitution) {
        regex.add(new RegexComponent()
            .setAction(action)
            .setExpression(expression)
            .setSubstitution(substitution));
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

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ValidatorSettings) {
            return ((ValidatorSettings) obj).name.equals(name);
        }
        return false;
    }
}
