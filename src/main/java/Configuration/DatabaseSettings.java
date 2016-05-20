package Configuration;

/**
 * @author Robin Duda
 *         Sets the database name and its connection string.
 */
public class DatabaseSettings {
    private String remote;
    private String name;

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
