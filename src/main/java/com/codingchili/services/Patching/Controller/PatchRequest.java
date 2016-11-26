package com.codingchili.services.patching.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codingchili.core.protocol.ClusterRequest;
import com.codingchili.core.protocol.Request;

import com.codingchili.services.patching.model.PatchFile;

import static com.codingchili.services.Shared.Strings.*;

/**
 * @author Robin Duda
 */
class PatchRequest extends ClusterRequest {
    private static final String MAX_VERSION = "999999999999999999999";

    PatchRequest(Request request) {
        super(request);
    }

    public void file(PatchFile file) {
        write(file);
    }

    public String file() {
        return data().getString(ID_FILE);
    }

    public String version() {
        String version = data().getString(ID_VERSION);

        if (version == null) {
            return MAX_VERSION;
        } else {
            return version;
        }
    }

    public int start() {
        return getRange()[0];
    }

    public int end() {
        return getRange()[1] + 1;
    }

    private int[] getRange() {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(data().getString(ID_RANGE));
        int[] range = new int[]{0, 0};
        int occurrence = 0;

        while (matcher.find()) {
            range[occurrence++] = Integer.parseInt(matcher.group(0));

            if (occurrence == 2) {
                break;
            }
        }
        return range;
    }

    public String webseedFile() {
        return file().replace(DIR_RESOURCES_QUOTED, "/");
    }
}
