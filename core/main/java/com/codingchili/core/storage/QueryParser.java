package com.codingchili.core.storage;

/**
 * @author Robin Duda
 *         <p>
 *         parses a query in string format.
 *         <p>
 *         Implementation is pluggable.
 */
public class QueryParser implements StringQueryParser {
    private static StringQueryParser parser = new QueryParser();

    @Override
    public QueryBuilder parse(QueryBuilder<?> builder, String expression) {
        return null; //implement parser, must create a NEW query.
    }

    /**
     * Evaluates the given query expression and creates an executable query.
     *
     * @param builder    the query builder to use when creating the query.
     * @param expression the expression to be parsed into the query builder.
     * @param <T>        type parameter of the builder to return/create.
     * @return a querybuilder ready for execution.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Storable> QueryBuilder<T> eval(QueryBuilder<?> builder, String expression) {
        return (QueryBuilder<T>) parser.parse(builder, expression);
    }

    /**
     * Replaces the default parser with the given string parser.
     *
     * @param parser the parser to use.
     */
    public void setParser(StringQueryParser parser) {
        QueryParser.parser = parser;
    }
}
