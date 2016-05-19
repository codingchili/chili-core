import Game.Model.Affliction;
import Game.Model.PlayerClass;
import Utilities.JsonFileStore;
import Utilities.Serializer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-25.
 */

public class JsonFileStoreTest {

    @Test
    public void testReadObject() throws IOException {
        JsonObject json = JsonFileStore.readObject("conf/game/class/paladin.json");
    }

    @Test
    public void testReadList() throws IOException {
        JsonArray json = JsonFileStore.readList("conf/game/player/affliction.json");
    }

    @Test
    public void readMissingFile() {
        try {
            JsonFileStore.readObject("missing/file.json");
            throw new RuntimeException("No exception on missing file.");
        } catch (IOException ignored) {
        }
    }

    @Test
    public void readDirectoryObjects() throws IOException {
        ArrayList<JsonObject> objects = JsonFileStore.readDirectoryObjects("conf/game/class/");
    }

    @Test
    public void testReadPaladin() throws IOException {
        JsonObject paladin = JsonFileStore.readObject("/conf/game/class/paladin.json");
        PlayerClass playerclass = (PlayerClass) Serializer.unpack(paladin, PlayerClass.class);
        System.out.println(Serializer.json(playerclass).encodePrettily());
    }

    @Test
    public void testReadAfflictions() throws IOException {
        JsonArray afflictions = JsonFileStore.readList("/conf/game/player/affliction.json");

        for (int i = 0; i < afflictions.size(); i++) {
            Affliction affliction = (Affliction) Serializer.unpack(afflictions.getJsonObject(i), Affliction.class);
            System.out.println(Serializer.json(affliction).encodePrettily());
        }
    }

    // todo not currently passing.
    // todo spellfactory, classfactory, attributefactory, mapping attributes with hashmaps, attributes no enum
    @Test
    public void testReadPlayerClasses() throws IOException {
        ArrayList<JsonObject> classes = JsonFileStore.readDirectoryObjects("conf/game/class/");

        for (JsonObject player : classes) {
            PlayerClass playerclass = (PlayerClass) Serializer.unpack(player, PlayerClass.class);
            System.out.println("packing " + player.getString("name"));
            System.out.println(Serializer.pack(playerclass));
        }
    }
}
