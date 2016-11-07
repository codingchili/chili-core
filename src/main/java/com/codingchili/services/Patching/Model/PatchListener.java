package com.codingchili.services.Patching.Model;

import com.codingchili.services.Patching.Configuration.PatchNotes;

/**
 * @author Robin Duda
 */
public interface PatchListener {
    void onPatchLoaded(PatchNotes notes);

    void onPatchReloading(PatchNotes notes);

    void onPatchReloaded(PatchNotes notes);

    void onFileLoaded(String path);
}
