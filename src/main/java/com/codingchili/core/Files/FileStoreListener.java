package com.codingchili.core.Files;

import java.nio.file.Path;

/**
 * @author Robin Duda
 */
interface FileStoreListener {
    void onFileModify(Path path);

    void onFileRemove(Path path);
}
