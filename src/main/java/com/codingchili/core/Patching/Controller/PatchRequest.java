package com.codingchili.core.Patching.Controller;

import com.codingchili.core.Patching.Model.PatchFile;
import com.codingchili.core.Protocols.Request;

/**
 * @author Robin Duda
 */
public interface PatchRequest extends Request {
    void file(PatchFile file);

    String file();

    String version();
}
