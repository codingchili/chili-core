package com.codingchili.services.Patching.Configuration;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
class NewsList {
    private ArrayList<NewsItem> list = new ArrayList<>();

    public ArrayList<NewsItem> getList() {
        return list;
    }

    public void setList(ArrayList<NewsItem> list) {
        this.list = list;
    }
}
