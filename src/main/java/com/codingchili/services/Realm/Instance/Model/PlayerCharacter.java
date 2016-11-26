package com.codingchili.services.realm.instance.model;

import com.codingchili.core.configuration.AttributeConfigurable;

/**
 * @author Robin Duda
 *         model for player characters.
 */
public class PlayerCharacter extends AttributeConfigurable {
    private String account;
    private String name;
    private String className;
    private Inventory inventory;

    public PlayerCharacter() {
    }

    public PlayerCharacter(PlayerCharacter template, String name, String className) {
        this.name = name;
        this.className = className;
        this.attributes = template.attributes;
        this.inventory = template.inventory;
    }

    public String getClassName() {
        return className;
    }

    public PlayerCharacter setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getName() {
        return name;
    }

    public PlayerCharacter setName(String name) {
        this.name = name;
        return this;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
