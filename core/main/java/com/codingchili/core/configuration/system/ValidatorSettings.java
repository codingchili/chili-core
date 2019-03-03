package com.codingchili.core.configuration.system;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.configuration.Configurable;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Contains a set of of named validators.
 */
public class ValidatorSettings implements Configurable {
    private Map<String, ParserSettings> validators = new HashMap<>();

    @Override
    public String getPath() {
        return PATH_VALIDATOR;
    }

    /**
     * @return all validators that are configured.
     */
    public Map<String, ParserSettings> getValidators() {
        return validators;
    }

    /**
     * @param validators sets the validator name:validator mappings.
     * @return fluent
     */
    public ValidatorSettings setValidators(Map<String, ParserSettings> validators) {
        this.validators = validators;
        return this;
    }

    @JsonIgnore
    public ParserSettings getValidator(String name) {
        if (validators.containsKey(name)) {
            return validators.get(name);
        } else {
            throw new IllegalArgumentException(getNoSuchValidator(name));
        }
    }

    /**
     * Adds a validator.
     *
     * @param name     the name of the parser settings to add
     * @param settings the settings for the validation to apply.
     * @return fluent
     */
    public ValidatorSettings add(String name, ParserSettings settings) {
        validators.put(name, settings);
        return this;
    }
}
