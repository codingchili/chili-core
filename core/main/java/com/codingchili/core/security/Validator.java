package com.codingchili.core.security;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.configuration.RegexComponent;
import com.codingchili.core.configuration.system.ValidatorSettings;
import com.codingchili.core.protocol.exception.RequestValidationException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import static com.codingchili.core.configuration.CoreStrings.PATH_VALIDATOR;
import static com.codingchili.core.configuration.CoreStrings.getNoSuchValidator;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Validates the contents of a json object according to the validation configuration.
 */
public class Validator implements Configurable {
    private static final String VALIDATION_FAILED_FOR_VALIDATOR = "Validation failed for validator '%s'.";
    private static final String REGEX_PLAINTEXT = "[A-Za-z0-9 \\-:&].*";
    private static final String REGEX_SPECIAL_CHARS = "[^A-Za-z0-9 \\-:&]";
    private Set<ValidatorSettings> settings = new HashSet<>();

    public Validator() {
    }

    @Override
    public String getPath() {
        return PATH_VALIDATOR;
    }

    /**
     * @param name the name of the validator to retrieve.
     * @return a validator matching the given name.
     */
    @JsonIgnore
    public ValidatorSettings get(String name) {
        return settings.stream()
            .filter(settings -> settings.getName().equals(name))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException(getNoSuchValidator(name)));
    }

    /**
     * Adds a validator.
     *
     * @param settings the settings for the validation to apply.
     * @return fluent
     */
    public Validator add(ValidatorSettings settings) {
        this.settings.add(settings);
        return this;
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
        for (ValidatorSettings settings : settings) {
            validateJsonObject(settings, json);
        }
        return json;
    }

    private Object validateFieldByType(ValidatorSettings settings, String fieldName, Object value) {
        if (value instanceof JsonObject) {
            return validateJsonObject(settings, (JsonObject) value);
        } else if (value instanceof JsonArray) {
            return validateJsonArray(settings, fieldName, (JsonArray) value);
        } else {
            return validateSimpleType(settings, fieldName, value);
        }
    }

    @SuppressWarnings("unchecked")
    private JsonArray validateJsonArray(ValidatorSettings settings, String fieldName, JsonArray value) {
        for (int i = 0; i < value.size(); i++) {
            value.getList().set(i, validateFieldByType(settings, fieldName, value.getValue(i)));
        }
        return value;
    }

    private JsonObject validateJsonObject(ValidatorSettings settings, JsonObject value) {
        for (String fieldName : value.fieldNames()) {
            value.put(fieldName, validateFieldByType(settings, fieldName, value.getValue(fieldName)));
        }
        return value;
    }

    private Object validateSimpleType(ValidatorSettings settings, String fieldName, Object value) {
        if (settings.isFieldValidated(fieldName)) {
            if (value instanceof String) {
                return validateString(settings, (String) value);
            } else {
                // only string type supports substitution.
                validateString(settings, value.toString());
                return value;
            }
        } else {
            return value;
        }
    }

    private String validateString(ValidatorSettings settings, String text) {
        if (text.length() < settings.getMinLength() || text.length() > settings.getMaxLength()) {
            throw RequestValidationException.lengthError(settings, text.length());
        }

        for (RegexComponent regex : settings.getRegex()) {
            switch (regex.getAction()) {
                case SUBSTITUTE:
                    text = text.replaceAll(regex.getExpression(),
                        Matcher.quoteReplacement(regex.getSubstitution()));

                    text = text.trim();
                    break;
                case REJECT:
                    if (text.matches(regex.getExpression())) {
                        fail(settings);
                    }
                    break;
                case ACCEPT:
                    if (!text.matches(regex.getExpression())) {
                        fail(settings);
                    }
                    break;
            }
        }
        return text;
    }

    private void fail(ValidatorSettings settings) {
        throw new RequestValidationException(String.format(
            VALIDATION_FAILED_FOR_VALIDATOR, settings.getName()));
    }
}
