package com.codingchili.patching.model;

import com.codingchili.patching.configuration.PatchNotes;

/**
 * @author Robin Duda
 */
public interface PatchListener {
    void onPatchLoaded(PatchNotes notes);

    void onPatchReloading(PatchNotes notes);

    void onPatchReloaded(PatchNotes notes);

    void onFileLoaded(String path);
}
