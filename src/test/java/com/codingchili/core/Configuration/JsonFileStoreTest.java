package com.codingchili.core.Configuration;

import com.codingchili.core.Realm.Model.Affliction;
import com.codingchili.core.Realm.Model.PlayerClass;
import com.codingchili.core.Protocols.Util.Serializer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Robin Duda
 *         tests the loading of json files used for configuration storage.
 */

public class JsonFileStoreTest {

    @Test
    public void testReadList() throws IOException {
        JsonFileStore.readList("conf/game/player/affliction.json");
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
        JsonFileStore.readDirectoryObjects("conf/game/class/");
    }

    @Test
    public void testReadPaladin() throws IOException {
        JsonObject paladin = JsonFileStore.readObject("conf/game/class/paladin.json");
        Serializer.unpack(paladin, PlayerClass.class);
    }

    @Test
    public void testReadAfflictions() throws IOException {
        JsonArray afflictions = JsonFileStore.readList("conf/game/player/affliction.json");

        for (int i = 0; i < afflictions.size(); i++) {
            Serializer.unpack(afflictions.getJsonObject(i), Affliction.class);
        }
    }

    @Test
    public void testReadPlayerClasses() throws IOException {
        ArrayList<JsonObject> classes = JsonFileStore.readDirectoryObjects("conf/game/class/");

        for (JsonObject player : classes) {
            Serializer.unpack(player, PlayerClass.class);
        }
    }
}
