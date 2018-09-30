package com.codingchili.core.storage;

import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 * <p>
 * parses a query in string format.
 * <p>
 * Implementation is pluggable.
 */
public class QueryParser<T extends Storable> implements StringQueryParser<T> {
    private static final String DECIMAL_SEPARATOR =
            new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator() + "";

    private static Map<String, BiConsumer<QueryBuilder<?>, Matcher>> operations = new ConcurrentHashMap<>();
    // matches a regex and everything within the following parenthesis, quoted strings and regular tokens.
    private static Pattern pattern = Pattern.compile("([0-9a-zA-Z.,]+)|\\(([^()]+)\\)|'([^']+)'|(\\(.+?\\))",
            Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);

    private Map<String, BiConsumer<QueryBuilder<?>, Matcher>> custom = new ConcurrentHashMap<>();
    private Supplier<QueryBuilder<T>> builders;

    static {
        operations.put(Query.QUERY, (builder, matcher) -> {
        });
        operations.put(Query.NAMED, (builder, matcher) -> {
            if (nextValue(matcher).equalsIgnoreCase(Query.QUERY)) {
                builder.setName(nextValue(matcher));
            }
        });

        // aliasing works, try and figure out a better option for query(null).
        operations.put(Query.ON, (builder, matcher) -> builder.on(nextValue(matcher)));
        operations.put(Query.IN, (builder, matcher) -> builder.in(nextComparableArray(matcher)));
        operations.put(Query.AND, (builder, matcher) -> builder.and(nextValue(matcher)));
        operations.put(Query.OR, (builder, matcher) -> builder.or(nextValue(matcher)));
        operations.put(Query.EQ, (builder, matcher) -> builder.equalTo(nextComparable(matcher)));
        operations.put(Query.STARTSWITH, (builder, matcher) -> builder.startsWith((nextValue(matcher))));
        operations.put(Query.ORDERBY, (builder, matcher) -> builder.orderBy(nextValue(matcher)));
        operations.put(Query.PAGE, (builder, matcher) -> builder.page(Integer.parseInt(nextValue(matcher))));
        operations.put(Query.PAGESIZE, (builder, matcher) -> builder.pageSize(Integer.parseInt(nextValue(matcher))));
        operations.put(Query.REGEX, (builder, matcher) -> builder.matches(nextValue(matcher)));
        operations.put(SortOrder.ASCENDING.name(), (builder, matcher) -> builder.order(SortOrder.ASCENDING));
        operations.put(SortOrder.DESCENDING.name(), (builder, matcher) -> builder.order(SortOrder.DESCENDING));
        operations.put(Query.BETWEEN, (builder, matcher) -> {
            long from = Long.parseLong(nextValue(matcher));
            if (matcher.find()) {
                builder.between(from, Long.parseLong(value(matcher)));
            }
        });
    }

    /**
     * @param matcher the matcher to get the next comparable. from/
     * @return a typed comparable.
     */
    public static Comparable nextComparable(Matcher matcher) {
        return toComparable(nextValue(matcher));
    }

    /**
     * Converts the given input string into a comparable depending on its content.
     *
     * @param string a string that may either represent an integer, double, boolean or a string.
     * @return the parsed data type.
     */
    public static Comparable toComparable(String string) {
        Comparable comparable = string;
        if (string.matches("true|false|TRUE|FALSE")) {
            comparable = Boolean.parseBoolean(string);
        } else {
            if (string.matches("[0-9]+")) {
                comparable = Integer.parseInt(string);
            } else {
                if (string.matches("[0-9.,]+")) {
                    string = string.replaceAll("[,.]", DECIMAL_SEPARATOR);
                    comparable = Double.parseDouble(string);
                }
            }
        }
        return comparable;
    }

    /**
     * @param matcher the matcher to retrieve comparables from.
     * @return an array of comparables, parsed from an input list "one,two,three" and separated by comma.
     */
    public static Comparable[] nextComparableArray(Matcher matcher) {
        List<Comparable> comparables = new ArrayList<>();

        for (String value : nextValue(matcher).split(",")) {
            comparables.add(toComparable(value));
        }

        return comparables.toArray(new Comparable[] {});
    }

    /**
     * @param builders a query builder to use.
     */
    public QueryParser(Supplier<QueryBuilder<T>> builders) {
        this.builders = builders;
    }

    @Override
    public QueryBuilder<T> parse(String expression) {
        // we can reuse the string-based query builder to verify Query.on(null)
        QueryBuilder<T> builder = builders.get();

        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String operation = value(matcher);

            if (custom.containsKey(operation)) {
                custom.get(operation).accept(builder, matcher);
            } else if (operations.containsKey(operation)) {
                operations.get(operation).accept(builder, matcher);
            } else {
                throw new CoreRuntimeException(String.format("Unknown query operator '%s'.", operation));
            }
        }
        return builder;
    }

    /**
     * Moves the matcher onto the next matching token, throwing an error if there is no next token.
     *
     * @param matcher the matcher to move.
     */
    public static void next(Matcher matcher) {
        if (!matcher.find()) {
            throw new CoreRuntimeException(String.format("Operator '%s' is missing an argument.", matcher.group()));
        }
    }

    /**
     * Move the parser and then extract the value of the token.
     *
     * @param matcher the matcher to extract the value from.
     * @return the unquoted value or the function value.
     * example: remove(id = 5) calls the 'remove' function, this method returns id = 5.
     */
    public static String nextValue(Matcher matcher) {
        next(matcher);
        return value(matcher);
    }

    /**
     * Extract the value of the token.
     *
     * @param matcher the matcher to extract the value from.
     * @return the unquoted value or the function value.
     * example: remove(id = 5) calls the 'remove' function, this method returns id = 5.
     */
    public static String value(Matcher matcher) {
        // find the specialized groups first that contains unquoted matches.
        for (int i = matcher.groupCount(); i >= 0; i--) {
            String value = matcher.group(i);
            if (value != null) {
                return value;
            }
        }
        return matcher.group();
    }

    /**
     * Allow injection of custom operations for all QueryParser instances.
     *
     * @return a map of defaults operations.
     */
    public static Map<String, BiConsumer<QueryBuilder<?>, Matcher>> defaults() {
        return operations;
    }

    /**
     * Allows injection of custom defaults into the current instance.
     *
     * @return a map of custom operations which may be modified.
     */
    public Map<String, BiConsumer<QueryBuilder<?>, Matcher>> customize() {
        return custom;
    }
}