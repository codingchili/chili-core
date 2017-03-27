package com.codingchili.core.context;

import static com.codingchili.core.configuration.CoreStrings.HELP;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.exception.CommandAlreadyExistsException;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author Robin Duda
 *
 * Tests for the CommandExecutor
 */
@RunWith(VertxUnitRunner.class)
public class CommandExecutorTest {
    private static final String COMMAND = "command";
    private static String MISSING_COMMAND = "command-missing";
    private CommandExecutor executor = new CommandExecutor();


    @Before
    public void setUp() {
        executor.add(() -> {}, HELP, "");
    }

    @Test
    public void testHiddenCommandHidden() {

    }

    @Test
    public void testIsHandledIfHandled(TestContext test) {
        test.assertTrue(executor.execute(HELP).isHandled());
    }

    @Test
    public void testNotHandledIfNotHandled(TestContext test) {
        test.assertFalse(executor.execute(MISSING_COMMAND).isHandled());
    }

    @Test
    public void testThrowsErrorIfAlreadyExists(TestContext test) {
        try {
            executor.add(() -> {
            }, HELP, "");
            test.fail("Test did not fail when adding an existing command.");
        } catch (CommandAlreadyExistsException ignored) {}
    }

    @Test
    public void testGetErrorContainsCommandName(TestContext test) {
        test.assertTrue(executor.execute(MISSING_COMMAND).getError().contains(MISSING_COMMAND));
    }

    @Test
    public void testCommandExecuted(TestContext test) {
        Async async = test.async();
        executor.add(async::complete, COMMAND, "");
        executor.execute(COMMAND);
    }
}
