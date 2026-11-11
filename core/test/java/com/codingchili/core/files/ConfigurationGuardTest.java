package com.codingchili.core.files;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.Timeout;
import org.junit.*;

import java.nio.file.*;
import java.util.*;

import com.codingchili.core.configuration.BaseConfigurable;
import com.codingchili.core.configuration.system.*;
import com.codingchili.core.context.CoreRuntimeException;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * Tests the configuration files of a service by its name ({@link ServiceFiles}) and the guard that reports a file
 * with secrets loaded outside of the files of the deployed services ({@link ConfigurationGuard}).
 */
public class ConfigurationGuardTest {
    private static final String DIR = "build/tmp/ConfigurationGuard/conf/";
    private static final String REALM = "com.example.realm.Service", ARENA = "com.example.arena.Service";

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    @Before
    public void setUp() {
        delete();
        var launcher = new LauncherSettings();
        launcher.setConfigurationDirectory(DIR);
        Configurations.put(launcher);
        var security = new SecuritySettings();
        security.setPath(PATH_SECURITY);
        var dependencies = new HashMap<String, AuthenticationDependency>();
        dependencies.put("service/[^/]*", new AuthenticationDependency().addToken("logging", "logging", "loggingSecret"));
        dependencies.put("realm/[^/]*", new AuthenticationDependency().addSecret("secret"));
        security.setDependencies(dependencies);
        Configurations.put(security);
    }

    @After
    public void tearDown() {
        Configurations.reset();
        delete();
    }

    @Test
    public void filesByName() {
        Assert.assertEquals("realm", ServiceFiles.name(REALM));
        Assert.assertEquals("website", ServiceFiles.name("com.codingchili.website.Service"));
        Assert.assertEquals("launcher", ServiceFiles.name("Launcher"));
        Assert.assertEquals("com.example.realm.", ServiceFiles.pkg(REALM));
        var realm = List.of(REALM);
        // its settings, a folder of its own name (the realms), and its folder in service/.
        Assert.assertTrue(ServiceFiles.reads(realm, "service/realm.yaml"));
        Assert.assertTrue(ServiceFiles.reads(realm, "realm/angel_oak.yaml"));
        Assert.assertTrue(ServiceFiles.reads(realm, "service/realm/extra.json"));
        // never another service's file or a name that only starts the same.
        Assert.assertFalse(ServiceFiles.reads(realm, "service/realms.yaml"));
        Assert.assertFalse(ServiceFiles.reads(realm, "service/arena.yaml"));
        Assert.assertFalse(ServiceFiles.reads(realm, "realms/angel_oak.yaml"));
        Assert.assertFalse(ServiceFiles.reads(realm, "service/realm.txt"));
        Assert.assertTrue(ServiceFiles.reads(List.of("com.example.authentication.Service"),
                "service/authentication/ticketfactory.yaml"));
        Assert.assertTrue(ServiceFiles.reads(List.of(ARENA, REALM), "service/arena.yaml"));
        Assert.assertEquals(List.of("conf/service/arena.yaml", "conf/service/arena/", "conf/arena/"),
                ServiceFiles.describe(ARENA));
    }

    @Test
    public void warnsAboutAFileOfAnotherService() {
        load("service/arena", new JsonObject().put("logging", "token"));
        // the realm's settings, loaded by the arena: the arena's image would not carry it.
        load("service/realm", new JsonObject().put("logging", "token").put("storage", "x"));
        // no secrets: not reported.
        load("service/plain", new JsonObject().put("storage", "x"));
        // a file with secret keys that the file does not hold: not reported.
        load("realm/empty", new JsonObject().put("name", "empty"));

        var reports = ConfigurationGuard.deployed(List.of(ARENA));
        Assert.assertEquals(reports.toString(), 1, reports.size());
        var report = reports.getFirst();
        Assert.assertTrue(report, report.contains(DIR + "service/realm.json"));
        Assert.assertTrue(report, report.contains("[logging]"));
        Assert.assertTrue(report, report.contains(ARENA));
        Assert.assertTrue(report, report.contains("config file = service name"));
        // deployed together the realm owns it.
        Assert.assertEquals(List.of(), ConfigurationGuard.deployed(List.of(ARENA, REALM)));
    }

    @Test
    public void strictFailsTheStart() {
        Configurations.put(new SystemSettings().setStrict(true));
        load("service/arena", new JsonObject().put("logging", "token"));
        load("service/realm", new JsonObject().put("logging", "token"));
        Assert.assertEquals(List.of(), ConfigurationGuard.deployed(List.of(ARENA, REALM)));
        try {
            ConfigurationGuard.deployed(List.of(ARENA));
            Assert.fail("strict: a file of a service that is not deployed must fail the start");
        } catch (CoreRuntimeException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("strict") && e.getMessage().contains("realm.json"));
        }
        // a later load is reported (an error log), it does not fail the running service.
        load("realm/angel_oak", new JsonObject().put("secret", "s"));
    }

    @Test
    public void strictFromTheSystemProperty() {
        Assert.assertFalse(new SystemSettings().strict());
        try {
            System.setProperty(PROPERTY_STRICT, "true");
            Assert.assertTrue(new SystemSettings().strict());
            System.setProperty(PROPERTY_STRICT, "false");
            Assert.assertFalse(new SystemSettings().setStrict(true).strict());
        } finally {
            System.clearProperty(PROPERTY_STRICT);
        }
    }

    private static void load(String name, JsonObject json) {
        String path = DIR + name + ".json";
        ConfigurationFactory.writeObject(json, path);
        Configurations.get(path, BaseConfigurable.class);
    }

    private static void delete() {
        try {
            Path root = Paths.get("build/tmp/ConfigurationGuard");
            if (Files.exists(root)) {
                try (var files = Files.walk(root)) {
                    files.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
