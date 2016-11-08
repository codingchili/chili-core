package com.codingchili.services.Realm;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.Files.Configurations;

import static com.codingchili.services.Shared.Strings.*;
import static com.codingchili.services.Shared.Strings.PATH_GAME_PLAYERTEMPLATE;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class SettingsOverrideTest {

    @Test
    public void getOverriddenFilePath() {
        Assert.assertEquals(
                PATH_GAME_PLAYERTEMPLATE.replace(PATH_GAME, PATH_GAME_OVERRIDE + "sample"),
                Configurations.override("conf/game/", "conf/realm/override/sample/", PATH_GAME_PLAYERTEMPLATE)
        );
    }

    @Test
    public void getNonOverridenFilePath() {
        Assert.assertEquals(
                PATH_GAME_PLAYERTEMPLATE,
                Configurations.override("conf/game/", "conf/realm/override/xxx/", PATH_GAME_PLAYERTEMPLATE)
        );
    }
}
