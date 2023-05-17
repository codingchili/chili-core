package com.codingchili.core.storage;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AttributeRegistration<Type> {
    private final Map<String, Attribute<?, String>> map = new HashMap<>();
    private final Class<Type> object;

    public AttributeRegistration(Class<Type> object) {
        this.object = object;
    }

    public <Value> AttributeRegistration<Type> single(Function<Type, Value> supplier, Class<Value> type, String fieldName) {
        var attribute = new SimpleAttribute<>(this.object, String.class, fieldName) {
            @Override
            public String getValue(Type object, QueryOptions queryOptions) {
                if (object == null) {
                    return null;
                } else {
                    var value = supplier.apply(object);
                    if (value == null) {
                        return "null";
                    } else {
                        return value.toString();
                    }
                }
            }
        };
        map.put(fieldName, attribute);
        return this;
    }

    public <Value> AttributeRegistration<Type> multi(Function<Type, Collection<Value>> supplier, Class<Value> type, String fieldName) {
        var attribute = new MultiValueAttribute<>(this.object, String.class, fieldName) {
            @Override
            public Iterable<String> getValues(Type indexing, QueryOptions queryOptions) {
                if (indexing == null) {
                    return Collections.emptyList();
                } else {
                    var collection = supplier.apply(indexing);

                    if (collection == null) {
                        return Collections.emptyList();
                    } else {
                        return collection
                                .stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                    }
                }
            }
        };
        map.put(fieldName, attribute);
        return this;
    }

    public Map<String, Attribute<?, String>> map() {
        return map;
    }

    public Class<?> object() {
        return object;
    }
}
