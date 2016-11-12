package com.codingchili.core.Context;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;

import com.codingchili.core.Configuration.System.LauncherSettings;
import com.codingchili.core.Logging.Logger;
import com.codingchili.core.Testing.LoggerMock;
import com.codingchili.core.Testing.MockLogListener;

import static com.codingchili.core.Configuration.Strings.HELP;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the command parsing functionality.
 */
@RunWith(VertxUnitRunner.class)
public class CommandExecutorTest {
    private static final String MISSING_COMMAND = "dev/null";

    @Test
    public void testWriteHelpMessage(TestContext test) {
        Async async = test.async();

        execute(HELP, log -> {
            if (log.contains(HELP))
                async.complete();
        });
    }

    @Test
    public void testIsHandledIfHandled(TestContext test) {
        test.assertTrue(execute(HELP).isHandled());
    }

    @Test
    public void testNotHandledIfNotHandled(TestContext test) {
        test.assertFalse(execute(MISSING_COMMAND).isHandled());
    }

    @Test
    public void testGetMessage(TestContext test) {
        test.assertTrue(execute(MISSING_COMMAND).getError().contains(HELP));
        test.assertTrue(execute(MISSING_COMMAND).getError().contains(MISSING_COMMAND));
    }

    @Test
    public void testConfiguredBlocksListed(TestContext test) {
        test.assertTrue(getOutput(HELP).contains("block1"));
        test.assertTrue(getOutput(HELP).contains("block2"));
    }

    @Test
    public void testConfiguredRemoteListed(TestContext test) {
        test.assertTrue(getOutput(HELP).contains("host1"));
        test.assertTrue(getOutput(HELP).contains("host2"));
        test.assertFalse(getOutput(HELP).contains("host3"));
    }

    private CommandExecutor execute(String arg) {
        return execute(arg, (log) -> {
        });
    }

    private CommandExecutor execute(String arg, MockLogListener listener) {
        return new CommandExecutor(new LaunchContextMock(new String[]{arg}, listener));
    }

    private String getOutput(String arg) {
        final String[] message = {""};


        execute(arg, logged -> {
            message[0] += logged;
        });

        return message[0];
    }

    private class LaunchContextMock extends LaunchContext {
        private LoggerMock logger;

        LaunchContextMock(String[] args, MockLogListener listener) {
            super(args);
            this.logger = new LoggerMock(listener);
        }

        @Override
        public LauncherSettings settings() {
            HashMap<String, List<String>> blocks = new HashMap<>();
            HashMap<String, String> hosts = new HashMap<>();
            LauncherSettings settings = new LauncherSettings();

            hosts.put("host1", "block1");
            hosts.put("host2", "block1");
            hosts.put("host3", "block-null");

            blocks.put("block1", new ArrayList<>());
            blocks.put("block2", new ArrayList<>());

            settings.setHosts(hosts);
            settings.setBlocks(blocks);

            return settings;
        }

        @Override
        public Logger console() {
            return logger;
        }
    }
}
