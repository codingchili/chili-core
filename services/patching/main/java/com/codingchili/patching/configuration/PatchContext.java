package com.codingchili.patching.configuration;

import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.patching.model.PatchKeeper;

import static com.codingchili.common.Strings.*;
import static com.codingchili.patching.configuration.PatchServerSettings.PATH_PATCHSERVER;

/**
 * @author Robin Duda
 */
public class PatchContext extends SystemContext {
    private PatchKeeper keeper;
    private Logger logger;

    public PatchContext(CoreContext core) {
        super(core);
        this.logger = core.logger(getClass());
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
        logger.event(LOG_PATCHER_LOADED)
                .put(ID_NAME, name)
                .put(LOG_VERSION, version).send();
    }

    public String directory() {
        return service().getDirectory();
    }

    public CachedFileStoreSettings fileStoreSettings() {
        return new CachedFileStoreSettings()
                .setDirectory(directory())
                .setGzip(gzip());
    }
}
