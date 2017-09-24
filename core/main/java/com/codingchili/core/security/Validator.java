package com.codingchili.core.security;

import com.codingchili.core.configuration.RegexComponent;
import com.codingchili.core.configuration.system.ParserSettings;
import com.codingchili.core.configuration.system.ValidatorSettings;
import com.codingchili.core.protocol.exception.RequestValidationException;
import io.vertx.core.json.JsonObject;

import java.util.function.Supplier;
import java.util.regex.Matcher;

/**
 * @author Robin Duda
 * <p>
 * Validates the contents of a json object according to the validation configuration.
 */
public class Validator {
    private static final String REGEX_PLAINTEXT = "[A-Za-z0-9 \\-:&].*";
    private static final String REGEX_SPECIAL_CHARS = "[^A-Za-z0-9 \\-:&]";
    private static final int MIN = 0;
    private static final int MAX = 1;
    private Supplier<ValidatorSettings> settings;

    /**
     * @param settings creates a new validator with the given settings.
     */
    public Validator(Supplier<ValidatorSettings> settings) {
        this.settings = settings;
    }

    /**
     * tests if a comparable objects string format contains characters that are not
     * in the accepted plaintext range of A-Z, a-z, 0-9, whitespace, dash, colon or ampersand.
     *
     * @param value the value to test.
     * @return true if the value is plaintext.
     */
    public static boolean plainText(Comparable value) {
        return value != null && value.toString().matches(REGEX_PLAINTEXT);
    }

    /**
     * Converts a string into a plaintext string, stripping any potential unsafe characters.
     *
     * @param input the string to be sanitized.
     * @return a plaintext string consisting of only A-Z, a-z, 0-9, whitespace, dash, colon or ampersand.
     */
    public static String toPlainText(String input) {
        return input.replaceAll(REGEX_SPECIAL_CHARS, "");
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
            for (ParserSettings validator : settings.get().getValidators().values()) {

                if (json.getValue(field) instanceof JsonObject) {
                    validate(json.getJsonObject(field), field);
                } else {
                    if (validator.keys.contains(field)) {
                        json.put(field, validate(validator, json.getValue(field)));
                    }
                }
            }
        }
        return json;
    }

    /**
     * Validates nested JSON objects within a JSON object.
     *
     * @param json      the json object to validate.
     * @param fieldName the handler of the field to validate.
     * @throws RequestValidationException when the evaluation is configured to reject a value
     */
    private void validate(JsonObject json, String fieldName) throws RequestValidationException {
        for (String field : json.fieldNames()) {
            if (json.getValue(field) instanceof JsonObject) {
                validate(json, fieldName + "." + field);
            } else {
                for (ParserSettings validator : settings.get().getValidators().values()) {
                    if (validator.keys.contains(fieldName + "." + field)) {
                        json.put(field, validate(validator, json.getValue(field)));
                    }
                }
            }
        }
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
}
