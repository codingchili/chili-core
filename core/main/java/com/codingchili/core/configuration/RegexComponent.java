package com.codingchili.core.configuration;

import com.codingchili.core.security.RegexAction;

/**
 * @author Robin Duda
 *         <p>
 *         Contains settings used for a single regex validator action.
 */
public class RegexComponent {
    public RegexAction action;
    public String line;
    public String replacement;

    public RegexComponent() {
    }

    public RegexAction getAction() {
        return action;
    }

    public RegexComponent setAction(RegexAction action) {
        this.action = action;
        return this;
    }

    public String getLine() {
        return line;
    }

    public RegexComponent setLine(String line) {
        this.line = line;
        return this;
    }

    public String getReplacement() {
        return replacement;
    }

    public RegexComponent setReplacement(String replacement) {
        this.replacement = replacement;
        return this;
    }
}
