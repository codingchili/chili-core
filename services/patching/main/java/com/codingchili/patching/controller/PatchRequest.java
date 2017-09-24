package com.codingchili.patching.controller;

import com.codingchili.core.files.CachedFile;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 * <p>
 * A request to the patch service.
 */
class PatchRequest extends RequestWrapper {
    private static final String MAX_VERSION = "999999999999999999999";

    PatchRequest(Request request) {
        super(request);
    }

    public void file(CachedFile file) {
        write(file);
    }

    public String file() {
        String file = data().getString(ID_FILE);
        if (file.startsWith(DIR_SEPARATOR)) {
            file = file.replace(DIR_SEPARATOR, "");
        }
        return file;
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
