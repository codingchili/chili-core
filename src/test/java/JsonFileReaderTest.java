import Game.Model.Affliction;
import Game.Model.PlayerClass;
import Utilities.JsonFileReader;
import Utilities.Serializer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-25.
 */

public class JsonFileReaderTest {

    @Test
    public void testReadObject() throws IOException {
        JsonObject json = JsonFileReader.readObject("conf/game/class/paladin.json");
    }

    @Test
    public void testReadList() throws IOException {
        JsonArray json = JsonFileReader.readList("conf/game/affliction.json");
    }

    @Test
    public void readMissingFile() {
        try {
            JsonFileReader.readObject("missing/file.json");
            throw new RuntimeException("No exception on missing file.");
        } catch (IOException ignored) {
        }
    }

    @Test
    public void readDirectoryObjects() throws IOException {
        ArrayList<JsonObject> objects = JsonFileReader.readDirectoryObjects("conf/game/class/");
    }

    @Test
    public void testReadPaladin() throws IOException {
        JsonObject paladin = JsonFileReader.readObject("/conf/game/class/paladin.json");
        PlayerClass playerclass = (PlayerClass) Serializer.unpack(paladin, PlayerClass.class);
        System.out.println(Serializer.json(playerclass).encodePrettily());
    }

    @Test
    public void testReadAfflictions() throws IOException {
        JsonArray afflictions = JsonFileReader.readList("/conf/game/affliction.json");

        for (int i = 0; i < afflictions.size(); i++) {
            Affliction affliction = (Affliction) Serializer.unpack(afflictions.getJsonObject(i), Affliction.class);
            System.out.println(Serializer.json(affliction).encodePrettily());
        }
    }

    // todo not currently passing.
    @Test
    public void testReadPlayerClasses() throws IOException {
        ArrayList<JsonObject> classes = JsonFileReader.readDirectoryObjects("conf/game/class/");

        for (JsonObject player : classes) {
            PlayerClass playerclass = (PlayerClass) Serializer.unpack(player, PlayerClass.class);
            System.out.println("packing " + player.getString("name"));
            System.out.println(Serializer.pack(playerclass));
        }
    }
}
