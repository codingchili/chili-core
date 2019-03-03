package com.codingchili.core.configuration.system;

import java.util.HashMap;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.storage.MongoDBMap;

import static com.codingchili.core.configuration.CoreStrings.PATH_STORAGE;

/**
 * configuration used by storages.
 */
public class StorageSettings implements Configurable {
    private static final String LOCALHOST = "localhost";
    private static final String CHILI = "chili";
    private HashMap<String, RemoteStorage> storage = new HashMap<>();
    private Integer maxResults = 128;
    private Integer minFeedbackChars = 3;

    public StorageSettings() {
        storage.put(MongoDBMap.class.getName(),
                new RemoteStorage(LOCALHOST, 27017, CHILI));

        // we cannot depend on the elastic storage from here, but we can still configure it.
        storage.put("com.codingchili.core.storage.ElasticMap",
                new RemoteStorage(LOCALHOST, 9200, "chili"));
    }

    @Override
    public String getPath() {
        return PATH_STORAGE;
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
     * @param storage a set of configurations mapped with plugins to set.
     */
    public void setStorage(HashMap<String, RemoteStorage> storage) {
        this.storage = storage;
    }

    /**
     * @param config the remote configuration to add
     * @param plugin the plugin as a string that the configuration applies to
     * @return fluent
     */
    public StorageSettings add(RemoteStorage config, Class plugin) {
        if (!storage.containsKey(plugin.getName())) {
            storage.put(plugin.getName(), config);
        } else {
            throw new UnsupportedOperationException(plugin.getName() + " is already configured. " +
                    "Use getSettingsForPlugin to modify.");
        }
        return this;
    }

    /**
     * @param plugin the plugin to get the configuration for
     * @return the configuration associated with the given plugin
     */
    public RemoteStorage getSettingsForPlugin(Class<?> plugin) {
        if (storage.containsKey(plugin.getName())) {
            return storage.get(plugin.getName());
        } else {
            return new RemoteStorage();
        }
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
