package Patching.Configuration;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
public class NewsList {
    private ArrayList<NewsItem> list = new ArrayList<>();

    public ArrayList<NewsItem> getList() {
        return list;
    }

    public void setList(ArrayList<NewsItem> list) {
        this.list = list;
    }
}
