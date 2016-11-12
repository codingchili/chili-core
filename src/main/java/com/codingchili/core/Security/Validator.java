package com.codingchili.core.Security;

import io.vertx.core.json.JsonObject;

import java.util.regex.Matcher;

import com.codingchili.core.Configuration.RegexComponent;
import com.codingchili.core.Configuration.System.ParserSettings;
import com.codingchili.core.Configuration.System.ValidatorSettings;
import com.codingchili.core.Exception.RequestValidationException;
import com.codingchili.core.Files.Configurations;

/**
 * @author Robin Duda
 */
public class Validator {
    private static final int MIN = 0;
    private static final int MAX = 1;

    private ValidatorSettings settings() {
        return Configurations.validator();
    }

    public JsonObject validate(JsonObject json) throws RequestValidationException {

        for (String field : json.fieldNames()) {
            for (ParserSettings validator : settings().getValidators().values()) {

                if (validator.keys.contains(field)) {
                    json.put(field, validate(validator, json.getValue(field)));
                }
            }
        }
        return json;
    }

    private Object validate(ParserSettings validator, Object value) throws RequestValidationException {
        if (value instanceof String) {
            return validateString(validator, (String) value);
        }
        return value;
    }

    private String validateString(ParserSettings validator, String text) throws RequestValidationException {
        if (text.length() < validator.length[MIN] || text.length() > validator.length[MAX]) {
            throw new RequestValidationException();
        }

        for (RegexComponent regex : validator.regex) {

            switch (regex.action) {
                case REJECT:
                    if (!text.matches(regex.line))
                        throw new RequestValidationException();
                    break;
                case REPLACE: text = text.replaceAll(regex.line, Matcher.quoteReplacement(regex.replacement));
                    break;
            }
        }
        return text.trim();
    }
}
