package com.codingchili.core.Protocols.Util;

import com.codingchili.core.Configuration.FileConfiguration;
import com.codingchili.core.Configuration.System.ValidatorSettings;
import com.codingchili.core.Configuration.System.ValidatorSettings.RegexComponent;
import com.codingchili.core.Configuration.System.ValidatorSettings.ValidationParser;
import io.vertx.core.json.JsonObject;

import java.util.regex.Matcher;

import static com.codingchili.core.Configuration.Strings.PATH_VALIDATOR;

/**
 * @author Robin Duda
 */
public class Validator {
    private static final int MIN = 0;
    private static final int MAX = 1;
    private final ValidatorSettings settings;

    public Validator() {
        settings = FileConfiguration.get(PATH_VALIDATOR, ValidatorSettings.class);
    }

    public JsonObject validate(JsonObject json) throws RequestValidationException {

        for (String field : json.fieldNames()) {
            for (ValidationParser validator : settings.getValidators().values()) {

                if (validator.keys.contains(field)) {
                    json.put(field, validate(validator, json.getValue(field)));
                }
            }
        }
        return json;
    }

    private Object validate(ValidationParser validator, Object value) throws RequestValidationException {
        if (value instanceof String) {
            return validateString(validator, (String) value);
        }
        return value;
    }

    private String validateString(ValidationParser validator, String text) throws RequestValidationException {
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

    public enum RegexAction {REJECT, REPLACE}

    public class RequestValidationException extends Exception {}
}
