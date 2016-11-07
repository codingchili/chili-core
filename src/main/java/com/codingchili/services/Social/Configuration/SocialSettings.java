package com.codingchili.services.Social.Configuration;

import com.codingchili.core.Configuration.ServiceConfigurable;
import com.codingchili.services.Shared.Strings;

/**
 * @author Robin Duda
 */
public class SocialSettings extends ServiceConfigurable {
    public static final String PATH_SOCIAL = Strings.getService("socialserver");
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
