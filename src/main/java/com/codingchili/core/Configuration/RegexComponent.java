package com.codingchili.core.Configuration;

import com.codingchili.core.Security.Validator;

/**
 * @author Robin Duda
 */
public class RegexComponent {
    public Validator.RegexAction action;
    public String line;
    public String replacement;

    public RegexComponent() {
    }

    public Validator.RegexAction getAction() {
        return action;
    }

    public void setAction(Validator.RegexAction action) {
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
