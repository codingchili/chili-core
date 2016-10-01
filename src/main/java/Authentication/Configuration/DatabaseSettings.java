package Authentication.Configuration;

import Protocols.Util.Serializer;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 *         Sets the database name and its connection string.
 */
public class DatabaseSettings {
    private String db_name;
    private String connection_string;
    private long pollRate;

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }

    public String getConnection_string() {
        return connection_string;
    }

    public void setConnection_string(String connection_string) {
        this.connection_string = connection_string;
    }

    public JsonObject toJson() {
        return Serializer.json(this);
    }

    public long getPollRate() {
        return pollRate;
    }

    public void setPollRate(long pollRate) {
        this.pollRate = pollRate;
    }
}
