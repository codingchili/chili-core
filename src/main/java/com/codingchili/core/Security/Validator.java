package com.codingchili.core.security;

import io.vertx.core.json.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codingchili.core.configuration.RegexComponent;
import com.codingchili.core.configuration.system.ParserSettings;
import com.codingchili.core.configuration.system.ValidatorSettings;
import com.codingchili.core.protocol.exception.RequestValidationException;
import com.codingchili.core.files.Configurations;

/**
 * @author Robin Duda
 *         <p>
 *         Validates the contents of a json object according to the validation configuration.
 */
public class Validator {
    private static final int MIN = 0;
    private static final int MAX = 1;

    private ValidatorSettings settings() {
        return Configurations.validator();
    }

    /**
     * Validates a json object using the regular expression that is configured for its
     * field names. Validation fails if evaluation is set to REJECT and the field value
     * is matching, if evaluation mode is set to REPLACE then the matching substring will be
     * replaced.
     *
     * @param json the json object to check field values on.
     * @return the json object with values that have failed validation replaced.
     * @throws RequestValidationException when the evaluation is configured to reject a value.
     */
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
                case REPLACE:
                    text = text.replaceAll(regex.line, Matcher.quoteReplacement(regex.replacement));
                    break;
            }
        }
        return text.trim();
    }

    /**
     * tests if a comparable objects string format contains characters that are not
     * in the accepted plaintext range of A-Z, a-z, whitespace and dash.
     *
     * @param value the value to test.
     * @return true if the value is plaintext.
     */
    public boolean plainText(Comparable value) {
        return value.toString().matches("[A-Za-z0-9 -].*");
    }
}
