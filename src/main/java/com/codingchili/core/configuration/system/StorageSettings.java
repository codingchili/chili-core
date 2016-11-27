package com.codingchili.core.configuration.system;

import java.util.HashMap;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.Strings;
import com.codingchili.core.storage.ElasticMap;
import com.codingchili.core.storage.MongoDBMap;

/**
 * @author Robin Duda
 *         <p>
 *         configuration used by storages.
 */
public class StorageSettings extends BaseConfigurable {
    private static final String LOCALHOST = "localhost";
    private static final String CHILI = "chili";
    private HashMap<String, RemoteStorage> storage = new HashMap<>();
    private Integer maxResults = 52;
    private Integer minFeedbackChars = 3;

    public StorageSettings() {
        super(Strings.PATH_STORAGE);

        storage.put(MongoDBMap.class.getCanonicalName(),
                new RemoteStorage(LOCALHOST, 27017, CHILI));
        storage.put(ElasticMap.class.getCanonicalName(),
                new RemoteStorage(LOCALHOST, 9300, CHILI));
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public HashMap<String, RemoteStorage> getStorage() {
        return storage;
    }

    public void setStorage(HashMap<String, RemoteStorage> storage) {
        this.storage = storage;
    }

    public Integer getMinFeedbackChars() {
        return minFeedbackChars;
    }

    public void setMinFeedbackChars(Integer minFeedbackChars) {
        this.minFeedbackChars = minFeedbackChars;
    }
}
