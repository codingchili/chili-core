package Configuration.Webserver;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
public class NewsList {
    private ArrayList<NewsItem> items = new ArrayList<>();

    public ArrayList<NewsItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<NewsItem> items) {
        this.items = items;
    }
}
