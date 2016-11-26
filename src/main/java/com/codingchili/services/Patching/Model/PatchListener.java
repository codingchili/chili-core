package com.codingchili.services.patching.model;

import com.codingchili.services.patching.configuration.PatchNotes;

/**
 * @author Robin Duda
 */
public interface PatchListener {
    void onPatchLoaded(PatchNotes notes);

    void onPatchReloading(PatchNotes notes);

    void onPatchReloaded(PatchNotes notes);

    void onFileLoaded(String path);
}
