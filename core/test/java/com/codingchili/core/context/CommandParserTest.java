package com.codingchili.core.context;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 * <p>
 * Tests for the parsing of command lines.
 */
@RunWith(VertxUnitRunner.class)
public class CommandParserTest {

    @Test
    public void parseSimpleCommand(TestContext test) {
        test.assertEquals(".command", parse(".command").getCommand().get());
    }

    @Test
    public void parseWithMultipleValues(TestContext test) {
        CommandParser parser = parse(".command --fruits apple orange banana");
        test.assertEquals(parser.getAllValues("--fruits").size(), 3);
        test.assertEquals(".command", parser.getCommand().get());
    }

    @Test
    public void parsePropertyNoValue(TestContext test) {
        CommandParser parser = parse(".command --name --foo");
        test.assertTrue(parser.hasProperty("--name"));
        test.assertTrue(parser.hasProperty("--foo"));
    }

    @Test
    public void checkIfMultiValue(TestContext test) {
        CommandParser parser = parse(".command --name a b c --foo single --monkey");
        test.assertTrue(parser.isMulti("--name"));
        test.assertFalse(parser.isMulti("--foo"));
        test.assertFalse(parser.isMulti("--monkey"));
    }

    private CommandParser parse(String text) {
        return new CommandParser(text.split(" "));
    }
}
