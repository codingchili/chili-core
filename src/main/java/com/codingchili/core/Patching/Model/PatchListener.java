package com.codingchili.core.Patching.Model;

/**
 * @author Robin Duda
 */
public interface PatchListener {
    void onPatchLoaded(String name, String version);

    void onPatchReloading(String name, String version);

    void onPatchReloaded(String name, String version);

    void onFileLoaded(String path);
}
