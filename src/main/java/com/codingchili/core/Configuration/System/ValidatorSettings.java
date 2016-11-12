package com.codingchili.core.Configuration.System;

import java.util.Map;

import com.codingchili.core.Configuration.WritableConfigurable;

/**
 * @author Robin Duda
 *
 * Contains a set of of named validators.
 */
public class ValidatorSettings extends WritableConfigurable {
    private Map<String, ParserSettings> validators;

    public Map<String, ParserSettings> getValidators() {
        return validators;
    }

    public void setValidators(Map<String, ParserSettings> validators) {
        this.validators = validators;
    }
}
