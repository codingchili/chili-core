package com.codingchili.core.context;

import io.vertx.core.Promise;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.Optional;

import com.codingchili.core.context.exception.CommandAlreadyExistsException;

import static com.codingchili.core.configuration.CoreStrings.HELP;
import static org.junit.Assert.fail;

/**
 * Tests for the CommandExecutor
 */
@RunWith(VertxUnitRunner.class)
public class CommandExecutorTest {
    private static final String TEST_COMMANDLINE_WITH_PARAM = "--test --property VALUE";
    private static final String TEST_COMMAND = "--test";
    private static final String HELP_ASYNC = "--help-async";
    private static final String COMMAND = "command";
    private static final String PROPERTY = "--property";
    private static final String VALUE = "VALUE";
    private static final String VALUE_2 = "VALUE2";
    private static String MISSING_COMMAND = "command-missing";
    private CommandExecutor executor = new DefaultCommandExecutor();
    private boolean executed = false;

    @Before
    public void setUp() {
        executor.add((executor) -> {
            executed = true;
            return LauncherCommandResult.SHUTDOWN;
        }, HELP, "");

        executor.add(((future, executor1) -> {
            future.complete();
            return null;
        }), HELP_ASYNC, "");
    }

    @Test
    public void testHandleSuccessfully(TestContext test) {
        executor.execute(HELP);
        test.assertTrue(executed);
    }

    @Test
    public void testHandleSuccessfullyAsync(TestContext test) {
        executor.execute(getCompleter(test.async()), HELP_ASYNC);
    }

    private Promise<CommandResult> getCompleter(Async async) {
        Promise<CommandResult> promise = Promise.<CommandResult>promise();
        promise.future().onComplete(done -> async.complete());
        return promise;
    }

    @Test
    public void testErrorWhenCommandMissing() {
        try {
            executor.execute(MISSING_COMMAND);
            fail("Executor did not throw exception when command is missing.");
        } catch (CoreRuntimeException ignored) {
        }
    }

    @Test
    public void testErrorWhenCommandMissingAsync(TestContext test) {
        Promise<CommandResult> promise = Promise.promise();
        Async async = test.async();

        promise.future().onComplete(done -> {
            if (done.failed()) {
                test.assertFalse(executed);
                async.complete();
            } else {
                test.fail("Test did not fail for missing command.");
            }
        });
        executor.execute(promise, MISSING_COMMAND);
    }

    @Test
    public void testCommandIsUndefined(TestContext test) {
        try {
            executor.execute();
            fail("Test did not fail for missing undefined command.");
        } catch (CoreRuntimeException e) {
            test.assertFalse(executor.getCommand().isPresent());
        }
    }

    @Test
    public void testGetPropertyValueWhenValueMissing(TestContext test) {
        executor.execute(HELP, PROPERTY);
        // value is not present; property is present.
        test.assertFalse(executor.getProperty(PROPERTY).isPresent());
        test.assertTrue(executor.hasProperty(PROPERTY));
    }

    @Test
    public void testCommandPropertyIsParsed(TestContext test) {
        executor.execute(HELP, PROPERTY, VALUE);
        Optional<String> propertyValue = executor.getProperty(PROPERTY);

        test.assertTrue(executor.hasProperty(PROPERTY));
        test.assertTrue(propertyValue.isPresent());
        test.assertEquals(VALUE, propertyValue.get());
    }

    @Test
    public void testMultipleCommandsSameNameReplacesExisting(TestContext test) {
        executor.execute(HELP, PROPERTY, VALUE, PROPERTY, VALUE_2);
        Optional<String> propertyValue = executor.getProperty(PROPERTY);

        test.assertTrue(executor.hasProperty(PROPERTY));
        test.assertTrue(propertyValue.isPresent());
        test.assertEquals(VALUE_2, propertyValue.get());
    }

    @Test
    public void testParametersPassedToCommand(TestContext test) {
        Async async = test.async();

        executor.add((executor) -> {
            test.assertTrue(executor.hasProperty(PROPERTY));
            test.assertTrue(executor.getProperty(PROPERTY).isPresent());
            test.assertEquals(VALUE, executor.getProperty(PROPERTY).get());
            async.complete();
            return LauncherCommandResult.SHUTDOWN;
        }, TEST_COMMAND, "").execute(TEST_COMMANDLINE_WITH_PARAM.split(" "));
    }

    @Test
    public void testThrowsErrorIfAlreadyExists(TestContext test) {
        try {
            executor.add((executor) -> LauncherCommandResult.SHUTDOWN, HELP, "");
            test.fail("Test did not fail when adding an existing command.");
        } catch (CommandAlreadyExistsException ignored) {
        }
    }

    @Test
    public void testGetErrorContainsCommandName() {
        try {
            executor.execute(MISSING_COMMAND);
        } catch (CoreRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains(MISSING_COMMAND));
        }
    }

    @Test
    public void testCommandExecuted(TestContext test) {
        Async async = test.async();
        executor.add((executor) -> {
            async.complete();
            return LauncherCommandResult.SHUTDOWN;
        }, COMMAND, "");
        executor.execute(COMMAND);
    }
}
