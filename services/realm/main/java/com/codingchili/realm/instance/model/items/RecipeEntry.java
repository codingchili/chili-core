package com.codingchili.realm.instance.model.items;

/**
 * @author Robin Duda
 */
public class RecipeEntry {
    private String name;
    private Integer quantity;
    private boolean consumed = true;

    public String getName() {
        return name;
    }

    public RecipeEntry setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public RecipeEntry setQuantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public RecipeEntry setConsumed(boolean consumed) {
        this.consumed = consumed;
        return this;
    }
}
