package com.codingchili.core.configuration.system;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.configuration.*;

import static com.codingchili.core.security.RegexAction.REJECT;
import static com.codingchili.core.security.RegexAction.REPLACE;

/**
 * @author Robin Duda
 *         <p>
 *         Contains a set of of named validators.
 */
public class ValidatorSettings extends BaseConfigurable {
    private Map<String, ParserSettings> validators = getDefaultValidators();

    public ValidatorSettings() {
        path = Strings.PATH_VALIDATOR;
    }

    public Map<String, ParserSettings> getValidators() {
        return validators;
    }

    public void setValidators(Map<String, ParserSettings> validators) {
        this.validators = validators;
    }

    private Map<String, ParserSettings> getDefaultValidators() {
        Map<String, ParserSettings> validators = new HashMap<>();

        validators.put("display-name", new ParserSettings()
                .addKey("username")
                .addKey("name")
                .length(4, 32)
                .addRegex(new RegexComponent()
                        .setAction(REJECT)
                        .setLine("[A-Z,a-z,0-9]*"))
        );


        validators.put("chat-messages", new ParserSettings()
                .addKey("message")
                .length(1, 76)
                .addRegex(new RegexComponent()
                        .setAction(REPLACE)
                        .setLine("(f..(c|k))")
                        .setReplacement("*^$#!?"))
        );

        return validators;
    }
}
