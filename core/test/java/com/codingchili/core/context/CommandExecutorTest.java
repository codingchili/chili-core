package com.codingchili.core.context;

import static com.codingchili.core.configuration.CoreStrings.HELP;
import static org.junit.Assert.fail;

import io.vertx.core.Future;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.exception.CommandAlreadyExistsException;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.Optional;

/**
 * @author Robin Duda
 *         <p>
 *         Tests for the CommandExecutor
 */
@RunWith(VertxUnitRunner.class)
public class CommandExecutorTest {
    private static final String HELP_ASYNC = "--help-async";
    private static final String COMMAND = "command";
    private static final String PROPERTY = "--property";
    private static final String VALUE = "VALUE";
    private static final String VALUE_2 = "VALUE2";
    private static String MISSING_COMMAND = "command-missing";
    private CommandExecutor executor = new CommandExecutor();
    private boolean executed = false;

    @Before
    public void setUp() {
        executor.add(() -> executed = true, HELP, "");
        executor.add(Future::complete, HELP_ASYNC, "");
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

    private Future<Void> getCompleter(Async async) {
        return Future.<Void>future().setHandler(done -> async.complete());
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
        Future<Void> future = Future.future();
        Async async = test.async();

        future.setHandler(done -> {
            if (done.failed()) {
                test.assertFalse(executed);
                async.complete();
            } else {
                test.fail("Test did not fail for missing command.");
            }
        });
        executor.execute(future, MISSING_COMMAND);
    }

    @Test
    public void testCommandIsUndefined(TestContext test) {
        try {
            executor.execute();
            fail("Test did not fail for missing command.");
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
    public void testThrowsErrorIfAlreadyExists(TestContext test) {
        try {
            executor.add(() -> {
            }, HELP, "");
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
        executor.add(async::complete, COMMAND, "");
        executor.execute(COMMAND);
    }
}
