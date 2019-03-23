package com.codingchili.core.configuration;

import com.codingchili.core.security.RegexAction;

/**
 * Contains settings used for a single regex validator action.
 */
public class RegexComponent {
    private RegexAction action;
    private String expression;
    private String substitution;

    public RegexComponent() {
    }

    /**
     * @return the type of action to be taken whenever the regex matches.
     */
    public RegexAction getAction() {
        return action;
    }

    /**
     * @param action the type of action to be taken whenever the regex matches.
     * @return fluent.
     */
    public RegexComponent setAction(RegexAction action) {
        this.action = action;
        return this;
    }

    /**
     * @return the regular expression.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression the regular expression used for matching input.
     * @return fluent.
     */
    public RegexComponent setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    /**
     * @return the replacement text used when action is  {@link RegexAction#SUBSTITUTE}.
     */
    public String getSubstitution() {
        return substitution;
    }

    /**
     * @param substitution the replacement text to use when the expression matches the input
     *                     and {@link RegexAction#SUBSTITUTE} is used.
     * @return fluent.
     */
    public RegexComponent setSubstitution(String substitution) {
        this.substitution = substitution;
        return this;
    }
}
