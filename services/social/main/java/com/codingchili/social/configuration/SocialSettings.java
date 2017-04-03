package com.codingchili.social.configuration;

import com.codingchili.common.Strings;

import com.codingchili.core.configuration.ServiceConfigurable;

/**
 * @author Robin Duda
 *
 * Settings for the social service.
 */
class SocialSettings extends ServiceConfigurable {
    static final String PATH_SOCIAL = Strings.getService("socialserver");
    private String message;

    /**
     * @return returns the configured message.
     */
    String getMessage() {
        return message;
    }

    /**
     * @param message sets the configured message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
