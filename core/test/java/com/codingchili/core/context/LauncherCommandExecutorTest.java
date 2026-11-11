package com.codingchili.core.context;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import com.codingchili.core.configuration.system.AuthenticationDependency;
import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.files.ConfigurationFactory;
import com.codingchili.core.security.exception.SecurityMissingDependencyException;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.testing.MockLogListener;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.context.LauncherCommandResult.SHUTDOWN;

/**
 * Tests the command parsing functionality.
 */
@RunWith(VertxUnitRunner.class)
public class LauncherCommandExecutorTest {
    private static final String MISSING_COMMAND = "dev/null";
    private static final String BLOCK_1 = "block1";
    private static final String BLOCK_2 = "block2";
    private static final String HOST_1 = "host1";
    private static final String HOST_2 = "host2";
    private static final String HOST_3 = "host3";
    private static final String HIDDEN = "hidden";

    @BeforeClass
    public static void setUp() {
        LauncherSettings launcher = Configurations.launcher();
        launcher.blocks().put(BLOCK_1, new ArrayList<>());
        launcher.blocks().put(BLOCK_2, new ArrayList<>());
        launcher.hosts().put(HOST_1, BLOCK_1);
        launcher.hosts().put(HOST_2, BLOCK_2);
    }

    @Test
    public void testWriteHelpMessage(TestContext test) {
        Async async = test.async();

        execute(HELP, log -> {
            if (log.contains(HELP))
                async.complete();
        });
    }

    @Test
    public void testHiddenCommandNotVisible(TestContext test) {
        new LauncherCommandExecutor(new LaunchContextMock(logged -> {
            test.assertFalse(logged.contains(HIDDEN));
        }).logger()).add(getHiddenCommand()).execute(HELP);
    }

    private Command getHiddenCommand() {
        return new BaseCommand((executor) -> SHUTDOWN, HIDDEN, HIDDEN).setVisible(false);
    }

    @Test
    public void testGetHelpMessage(TestContext test) {
        new LauncherCommandExecutor(
                new LaunchContextMock((line) -> {}).logger())
                .execute(MISSING_COMMAND)
                .onComplete(test.asyncAssertFailure(e -> {
                    test.assertTrue(e.getMessage().contains(HELP));
                    test.assertTrue(e.getMessage().contains(MISSING_COMMAND));
                }));
    }

    @Test
    public void testFailedGenerateFailsTheCommandAndWritesNothing(TestContext test) {
        // a token whose issuer has no such secret: the generator fails before it writes anything.
        var security = Configurations.security();
        String dependency = "system/launcher";
        String file = PATH_LAUNCHER;
        // the file must exist for the dependency to match it (a fresh checkout has no conf/ yet).
        boolean created = !ConfigurationFactory.exists(file);
        if (created) {
            ConfigurationFactory.writeObject(Configurations.launcher().serialize(), file);
        }
        String before = ConfigurationFactory.readObject(file).encode();
        security.getDependencies().put(dependency,
                new AuthenticationDependency().addToken("token", "missing-service", "missing-secret"));

        new LauncherCommandExecutor(new LaunchContextMock((line) -> {}).logger())
                .execute(GENERATE)
                .onComplete(test.asyncAssertFailure(e -> {
                    security.getDependencies().remove(dependency);
                    test.assertTrue(e instanceof SecurityMissingDependencyException, String.valueOf(e));
                    test.assertEquals(before, ConfigurationFactory.readObject(file).encode());
                    if (created) {
                        ConfigurationFactory.delete(file);
                    }
                }));
    }

    @Test
    public void testKeygenRefusesToReplaceAKeyWithoutForce(TestContext test) throws Exception {
        var path = java.nio.file.Files.createTempDirectory("keygen").resolve("node.key");
        System.setProperty(PROPERTY_NODE_KEY, path.toString());
        var executor = new LauncherCommandExecutor(new LaunchContextMock((line) -> {}).logger());

        executor.execute(KEYGEN).compose(written -> {
            String first = com.codingchili.core.security.NodeKey.read(path).publicKey();
            return new LauncherCommandExecutor(new LaunchContextMock((line) -> {}).logger()).execute(KEYGEN)
                    .transform(refused -> {
                        test.assertTrue(refused.failed());
                        test.assertTrue(refused.cause().getMessage().contains(KEYGEN_FORCE));
                        test.assertEquals(first, com.codingchili.core.security.NodeKey.read(path).publicKey());
                        return new LauncherCommandExecutor(new LaunchContextMock((line) -> {}).logger())
                                .execute(KEYGEN, KEYGEN_FORCE);
                    }).map(forced -> {
                        test.assertNotEquals(first, com.codingchili.core.security.NodeKey.read(path).publicKey());
                        return forced;
                    });
        }).onComplete(test.asyncAssertSuccess(done -> {
            System.clearProperty(PROPERTY_NODE_KEY);
            path.toFile().delete();
            path.getParent().toFile().delete();
        }));
    }

    @Test
    public void testConfiguredBlocksListed(TestContext test) {
        System.err.println(getOutput(HELP));
        test.assertTrue(getOutput(HELP).contains(BLOCK_1));
        test.assertTrue(getOutput(HELP).contains(BLOCK_2));
    }

    @Test
    public void testConfiguredRemoteListed(TestContext test) {
        test.assertTrue(getOutput(HELP).contains(HOST_1));
        test.assertTrue(getOutput(HELP).contains(HOST_2));
        test.assertFalse(getOutput(HELP).contains(HOST_3));
    }

    private Future<CommandResult> execute(String arg) {
        return execute(arg, (log) -> {
        });
    }

    private Future<CommandResult> execute(String arg, MockLogListener listener) {
        return new LauncherCommandExecutor(
                new LaunchContextMock(listener).logger()).execute(arg);
    }

    private String getOutput(String arg) {
        final String[] message = {""};


        execute(arg, logged -> {
            message[0] += logged + "\n";
        });

        return message[0];
    }
}
