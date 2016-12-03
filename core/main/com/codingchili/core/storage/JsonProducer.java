package com.codingchili.core.storage;

/**
 * @author Robin Duda
 *         <p>
 *         Implemented by a Json message producer to indicate that it can handle
 *         a json output storage.
 */
public interface JsonProducer {

    /**
     * Adds a storage which the producer will push all produced items to.
     *
     * @param storage the storage where produced messages are inserted.
     */
    void addConsumer(JsonStorage storage);
}
