package com.codingchili.core.Patching.Controller;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Patching.Model.PatchFile;
import com.codingchili.core.Protocols.ClusterRequest;
import com.codingchili.core.Protocols.Request;

/**
 * @author Robin Duda
 */
class PatchRequest extends ClusterRequest {

    PatchRequest(Request request) {
        super(request);
    }

    public void file(PatchFile file) {
        write(file);
    }

    public String file() {
        return data().getString(Strings.ID_FILE);
    }

    public String version() {
        return data().getString(Strings.ID_VERSION);
    }
}
