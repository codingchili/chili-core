package com.codingchili.core.storage;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Robin Duda
 *         <p>
 *         parses a query in string format.
 *         <p>
 *         Implementation is pluggable.
 */
public class QueryParser<T extends Storable> implements StringQueryParser<T> {
    private AsyncStorage<T> store;

    public QueryParser(AsyncStorage<T> store) {
        this.store = store;
    }

    @Override
    public Handler<AsyncResult<Collection<T>>> parse(String expression) {
        throw new NotImplementedException();
    }

    private static String name(String expression) {
        Pattern pattern = Pattern.compile("([a-zA-Z ])+(?::)");
        Matcher matcher = pattern.matcher(expression);
        matcher.group();
        if (matcher.groupCount() > 0) {
            return matcher.group(0);
        } else {
            return QueryParser.class.getSimpleName();
        }
    }
}
