package com.codingchili.services.Patching.Configuration;

import io.vertx.core.Vertx;

import com.codingchili.core.Context.ServiceContext;
import com.codingchili.core.Files.Configurations;

import com.codingchili.services.Patching.Model.PatchKeeper;

import static com.codingchili.services.Patching.Configuration.PatchServerSettings.PATH_PATCHSERVER;
import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
public class PatchContext extends ServiceContext {
    private PatchKeeper keeper;

    public PatchContext(Vertx vertx) {
        super(vertx);
        this.keeper = new PatchKeeper(this);
    }

    protected PatchServerSettings service() {
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
