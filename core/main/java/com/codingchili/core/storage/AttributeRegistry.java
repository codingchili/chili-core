package com.codingchili.core.storage;

import com.googlecode.cqengine.attribute.Attribute;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AttributeRegistry {
    private static final Map<String, Map<String, Attribute<?, String>>> registry = new ConcurrentHashMap<>();

    public static <T> Attribute<T, String> get(Class<?> objectKey, String fieldName) {
        if (registry.containsKey(objectKey.getName())) {
            var object = registry.get(objectKey.getName());
            if (object.containsKey(fieldName)) {
                return (Attribute<T, String>) object.get(fieldName);
            } else {
                throw new AttributeNotRegisteredException(objectKey, fieldName);
            }
        } else {
            throw new StorableNotRegisteredException(objectKey);
        }
    }

    @SuppressWarnings("unchecked")
    public static <Type> void register(Consumer<AttributeRegistration<Type>> consumer, Class<Type> object) {
        var registration = new AttributeRegistration<>(object);

        if (registry.containsKey(object.getName())) {
            registration.load(registry.get(object.getName()));
        }

        if (Storable.class.isAssignableFrom(object)) {
            ((AttributeRegistration<Storable>) registration).single(Storable::getId, String.class, "id");
        }
        consumer.accept(registration);
        registry.put(registration.object().getName(), registration.map());
    }
}
