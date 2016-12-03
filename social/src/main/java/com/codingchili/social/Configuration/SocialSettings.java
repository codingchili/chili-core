package com.codingchili.social.configuration;

import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.common.Strings;

/**
 * @author Robin Duda
 */
class SocialSettings extends ServiceConfigurable {
    static final String PATH_SOCIAL = Strings.getService("socialserver");
    private String message;

    String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
