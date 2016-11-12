package com.codingchili.core.Configuration;

import com.codingchili.core.Security.RegexAction;

/**
 * @author Robin Duda
 *
 * Contains settings used for a single regex validator action.
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

    public void setAction(RegexAction action) {
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
