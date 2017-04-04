package com.codingchili.patching.configuration;

import com.codingchili.patching.model.PatchKeeper;
import io.vertx.core.Vertx;

import com.codingchili.core.context.ServiceContext;
import com.codingchili.core.files.Configurations;

import static com.codingchili.common.Strings.*;
import static com.codingchili.patching.configuration.PatchServerSettings.PATH_PATCHSERVER;

/**
 * @author Robin Duda
 */
public class PatchContext extends ServiceContext {
    private PatchKeeper keeper;

    public PatchContext(Vertx vertx) {
        super(vertx);
        this.keeper = new PatchKeeper(this);
    }

    public PatchServerSettings service() {
        return Configurations.get(PATH_PATCHSERVER, PatchServerSettings.class);
    }

    public PatchKeeper getPatchKeeper() {
        return keeper;
    }

    public PatchNotes notes() {
        return service().getPatchNotes();
    }

    public GameInfo gameinfo() {
        return service().getGameinfo();
    }

    public NewsList news() {
        return service().getNews();
    }

    public boolean gzip() {
        return service().isGzip();
    }

    public void onPatchLoaded(String name, String version) {
        log(event(LOG_PATCHER_LOADED)
                .put(ID_NAME, name)
                .put(LOG_VERSION, version));
    }

    public String directory() {
        return service().getDirectory();
    }
}
