package com.codingchili.core.Configuration.System;

import com.codingchili.core.Configuration.LoadableConfigurable;
import com.codingchili.core.Protocols.Util.Validator.RegexAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codingchili.core.Configuration.Strings.PATH_VALIDATOR;

/**
 * @author Robin Duda
 */
public class ValidatorSettings implements LoadableConfigurable {
    private Map<String, ValidationParser> validators;

    public Map<String, ValidationParser> getValidators() {
        return validators;
    }

    public void setValidators(Map<String, ValidationParser> validators) {
        this.validators = validators;
    }

    @Override
    public String getPath() {
        return PATH_VALIDATOR;
    }

    public static class ValidationParser {
        public List<RegexComponent> regex;
        public ArrayList<String> keys;
        public int[] length;

        public ValidationParser() {}

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

    public static class RegexComponent {
        public RegexAction action;
        public String line;
        public String replacement;

        public RegexComponent() {}

        public RegexAction getAction() {
            return action;
        }

        public void setAction(RegexAction action) {
            this.action = action;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public String getReplacement() {
            return replacement;
        }

        public void setReplacement(String replacement) {
            this.replacement = replacement;
        }
    }
}
