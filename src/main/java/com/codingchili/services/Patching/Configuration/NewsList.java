package com.codingchili.services.Patching.Configuration;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
class NewsList {
    private ArrayList<NewsItem> list = initializeNewsList();

    public ArrayList<NewsItem> getList() {
        return list;
    }

    public void setList(ArrayList<NewsItem> list) {
        this.list = list;
    }

    private ArrayList<NewsItem> initializeNewsList() {
        ArrayList<NewsItem> list = new ArrayList<>();
        list.add(new NewsItem());
        return list;
    }
}
