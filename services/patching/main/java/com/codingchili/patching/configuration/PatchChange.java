package com.codingchili.patching.configuration;

import java.util.ArrayList;

/**
 * @author Robin Duda
 * Contains a changelist displayed on the website.
 */
class PatchChange {
    private String title;
    private ArrayList<String> list;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }
}
