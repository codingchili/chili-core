package Configuration;

import Utilities.JsonFileStore;
import Utilities.RemoteAuthentication;
import Utilities.Serializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Robin on 2016-05-05.
 */
@JsonIgnoreProperties({"realms", "path", "name"})
public class GameServerSettings implements Configurable {
    static final String GAMESERVER_PATH = "conf/system/gameserver.json";
    static final String REALM_PATH = "conf/realm/";
    private RemoteAuthentication logserver;
    private ArrayList<RealmSettings> realms = new ArrayList<>();

    public GameServerSettings() throws IOException {
        this.loadRealms();
    }

    private void loadRealms() throws IOException {
        ArrayList<JsonObject> configurations = JsonFileStore.readDirectoryObjects(REALM_PATH);

        for (JsonObject configuration : configurations)
            realms.add((RealmSettings) Serializer.unpack(configuration, RealmSettings.class));
    }

    public ArrayList<RealmSettings> getRealms() {
        return realms;
    }

    public RemoteAuthentication getLogserver() {
        return logserver;
    }

    protected void setLogserver(RemoteAuthentication logserver) {
        this.logserver = logserver;
    }

    @Override
    public String getPath() {
        return GAMESERVER_PATH;
    }

    @Override
    public String getName() {
        return logserver.getSystem();
    }
}
