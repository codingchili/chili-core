package com.codingchili.social.configuration;

import io.vertx.core.Vertx;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;

import static com.codingchili.social.configuration.SocialSettings.PATH_SOCIAL;

/**
 * @author Robin Duda
 */
public class SocialContext extends ServiceContext {

    public SocialContext(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected SocialSettings service() {
        return Configurations.get(PATH_SOCIAL, SocialSettings.class);
    }

    public String message() {
        return service().getMessage();
    }
}
