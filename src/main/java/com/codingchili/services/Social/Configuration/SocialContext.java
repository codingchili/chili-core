package com.codingchili.services.Social.Configuration;

import io.vertx.core.Vertx;

import com.codingchili.core.Files.Configurations;

import com.codingchili.core.Context.ServiceContext;

import static com.codingchili.services.Social.Configuration.SocialSettings.PATH_SOCIAL;

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
