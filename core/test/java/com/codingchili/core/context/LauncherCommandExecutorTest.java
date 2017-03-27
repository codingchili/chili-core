package com.codingchili.core.context;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.configuration.system.LauncherSettings;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.testing.MockLogListener;

import static com.codingchili.core.configuration.CoreStrings.HELP;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the command parsing functionality.
 */
@RunWith(VertxUnitRunner.class)
public class LauncherCommandExecutorTest {
    private static final String MISSING_COMMAND = "dev/null";
    private static final String BLOCK_1 = "block1";
    private static final String BLOCK_2 = "block2";
    private static final String HOST_1 = "host1";
    private static final String HOST_2 = "host2";
    private static final String HOST_3 = "host3";

    @BeforeClass
    public static void setUp(TestContext test) {
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
    public void testGetHelpMessage(TestContext test) {
        test.assertTrue(execute(MISSING_COMMAND).getError().contains(HELP));
        test.assertTrue(execute(MISSING_COMMAND).getError().contains(MISSING_COMMAND));
    }

    @Test
    public void testConfiguredBlocksListed(TestContext test) {
        System.out.println(getOutput(HELP));
        test.assertTrue(getOutput(HELP).contains(BLOCK_1));
        test.assertTrue(getOutput(HELP).contains(BLOCK_2));
    }

    @Test
    public void testConfiguredRemoteListed(TestContext test) {
        test.assertTrue(getOutput(HELP).contains(HOST_1));
        test.assertTrue(getOutput(HELP).contains(HOST_2));
        test.assertFalse(getOutput(HELP).contains(HOST_3));
    }

    private CommandExecutor execute(String arg) {
        return execute(arg, (log) -> {
        });
    }

    private CommandExecutor execute(String arg, MockLogListener listener) {
        return new LauncherCommandExecutor(new LaunchContextMock(listener).console()).execute(arg);
    }

    private String getOutput(String arg) {
        final String[] message = {""};


        execute(arg, logged -> {
            message[0] += logged;
        });

        return message[0];
    }
}
