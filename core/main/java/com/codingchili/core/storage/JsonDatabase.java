package com.codingchili.core.storage;

import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.files.exception.NoSuchResourceException;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class JsonDatabase<Value> {
    private Map<String, Value> map = new ConcurrentHashMap<>();

    public JsonDatabase(Class<Value> value, String path) {
        try {
            var map = ConfigurationFactory.readObject(path)
                    .getJsonObject("map");

            var entries = map.fieldNames();

            for (String id : entries) {
                var entry = map.getJsonObject(id);
                var object = Serializer.unpack(entry, value);
                map.put(id, object);
            }
        } catch (NoSuchResourceException e) {
            // instantiate new.
        }
    }

    public void clear() {
        map.clear();
    }

    public Value get(String id) {
        return map.get(id);
    }

    public void put(String id, Value value) {
        map.put(id, value);
    }

    public void remove(String id) {
        map.remove(id);
    }

    public JsonObject toJson() {
        return Serializer.json(this);
    }

    public Integer size() {
        return map.size();
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public Stream<Map.Entry<String, Value>> stream() {
        return map.entrySet().stream();
    }

    public Map<String, Value> getMap() {
        return map;
    }

    public void setMap(Map<String, Value> value) {
        map = value;
    }
}
