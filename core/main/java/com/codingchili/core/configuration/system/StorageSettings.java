package com.codingchili.core.configuration.system;

import java.util.HashMap;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.CoreStrings;
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
    private Integer maxResults = 128;
    private Integer minFeedbackChars = 3;

    public StorageSettings() {
        super(CoreStrings.PATH_STORAGE);

        storage.put(MongoDBMap.class.getCanonicalName(),
                new RemoteStorage(LOCALHOST, 27017, CHILI));
        storage.put(ElasticMap.class.getCanonicalName(),
                new RemoteStorage(LOCALHOST, 9300, CHILI));
    }

    /**
     * @return the max number of results that should be returned for queries.
     * This is a recommendation only, should not be applied for enumeration queries.
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * @param maxResults sets the recommended number of results to return from queries.
     */
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * @return the configuration of a remote storage.
     */
    public HashMap<String, RemoteStorage> getStorage() {
        return storage;
    }

    /**
     * @param config the remote configuration to add
     * @param plugin the plugin as a string that the configuration applies to
     * @return fluent
     */
    public StorageSettings add(RemoteStorage config, String plugin) {
        storage.put(plugin, config);
        return this;
    }

    /**
     * @param plugin the plugin to get the configuration for
     * @return the configuration associated with the given plugin
     */
    public RemoteStorage storage(String plugin) {
        if (storage.containsKey(plugin)) {
            return storage.get(plugin);
        } else {
            return new RemoteStorage();
        }
    }

    /**
     * @param storage a set of configurations mapped with plugins to set.
     */
    public void setStorage(HashMap<String, RemoteStorage> storage) {
        this.storage = storage;
    }

    /**
     * @return the minimum number of characters that must be provided in order to
     * query for auto-completion. This is a recommendation only.
     */
    public Integer getMinFeedbackChars() {
        return minFeedbackChars;
    }

    /**
     * @param minFeedbackChars set the recommendation minimum number of characters
     *                         that is required before returning results for auto-completion.
     */
    public void setMinFeedbackChars(Integer minFeedbackChars) {
        this.minFeedbackChars = minFeedbackChars;
    }
}
