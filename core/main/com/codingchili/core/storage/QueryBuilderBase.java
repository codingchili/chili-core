package com.codingchili.core.storage;

/**
 * @author Robin Duda
 */
abstract class QueryBuilderBase<Value> implements QueryBuilder<Value> {
    protected String attribute;
    private int pageSize = 128;
    private int page = 0;

    @Override
    public QueryBuilder page(int page) {
        this.page = page;
        return this;
    }

    @Override
    public QueryBuilder pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }
}
