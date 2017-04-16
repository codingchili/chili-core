package com.codingchili.core.security;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import com.codingchili.core.configuration.RegexComponent;
import com.codingchili.core.configuration.system.ParserSettings;
import com.codingchili.core.configuration.system.ValidatorSettings;
import com.codingchili.core.protocol.exception.RequestValidationException;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.security.RegexAction.*;

/**
 * @author Robin Duda
 *         <p>
 *         Tests the validation mechanism.
 */
@RunWith(VertxUnitRunner.class)
public class ValidatorTest {
    private static final String NESTED = "nested";
    private static final String KEY = "key";
    private static Validator validator;
    private static ValidatorSettings settings;

    @Before
    public void setUp() {
        settings = new ValidatorSettings().setValidators(getDefaultValidators())
                .add(NESTED,
                        new ParserSettings()
                                .length(0, 8)
                                .addKey(NESTED + "." + KEY));
        validator = new Validator(() -> settings);
    }

    @Test
    public void testMessageIsFiltered() throws RequestValidationException {
        Assert.assertEquals("test " +
                settings.getValidators().get("chat-messages").regex.get(0).replacement +
                " hello", getMessage("test f**k hello"));
    }

    @Test
    public void testMessageNotFiltered() throws RequestValidationException {
        Assert.assertEquals("hello test meow", getMessage("hello test meow"));
    }

    @Test
    public void testUsernameFilterRejected(TestContext test) {
        try {
            getUser("invalid string with space.. #@!");
            test.fail("Validation error to detect malicious input.");
        } catch (RequestValidationException ignored) {
        }
    }

    @Test
    public void testPlainText(TestContext test) {
        test.assertTrue(plaintext("abc102"));
        test.assertTrue(plaintext("abc 102"));
        test.assertTrue(plaintext("abc-102"));
        test.assertTrue(plaintext(1000));
        test.assertTrue(plaintext(1000L));
        test.assertTrue(plaintext(new Byte("0")));

        String[] invalid = {"?", "_", "%", "^", ".", "*"};

        for (String character : invalid) {
            test.assertFalse(plaintext(character));
        }
    }

    private boolean plaintext(Comparable comparable) {
        return Validator.plainText(comparable);
    }

    @Test
    public void testUsernameValidated() throws RequestValidationException {
        getUser("myFirstUserName");
        getUser("anotherFantasticUsername");
        getUser("000TheUserName123");
    }

    @Test
    public void testMinimumLengthIsEnforced(TestContext test) {
        String tooShort = "i";
        try {
            getUser(tooShort);
            test.fail("Too short string does not fail to validate.");
        } catch (RequestValidationException ignored) {
        }
    }

    @Test
    public void testMaximumLengthIsEnforced(TestContext test) {
        String tooLong = new String(new char[100]).replace("\0", "X");

        try {
            getMessage(tooLong);
            test.fail("Too long string does not fail to validate.");
        } catch (RequestValidationException ignored) {
        }
    }

    @Test
    public void testToPlainText(TestContext test) {
        String[] texts = {"!@#$%^&*()_?\\[]{}"};

        for (String text : texts) {
            test.assertTrue(plaintext(validator.toPlainText(text)));
        }
    }

    @Test
    public void testNestedAttributeOk(TestContext test) throws RequestValidationException {
        validator.validate(getNestedObject("ok"));
    }

    @Test
    public void testNestedAttributeFail(TestContext test) throws RequestValidationException {
        try {
            validator.validate(getNestedObject("too-long-should-fail"));
            test.fail("validation did not fail for nested object.");
        } catch (RequestValidationException ignored) {
        }
    }

    private JsonObject getNestedObject(String value) {
        return new JsonObject().put(NESTED, new JsonObject().put(KEY, value));
    }

    private String getMessage(String message) throws RequestValidationException {
        return validate(new JsonObject().put(PROTOCOL_MESSAGE, message)).getString(PROTOCOL_MESSAGE);
    }

    private String getUser(String user) throws RequestValidationException {
        return validate(new JsonObject().put(ID_NAME, user)).getString(ID_NAME);
    }

    private JsonObject validate(JsonObject json) throws RequestValidationException {
        return validator.validate(json);
    }

    private Map<String, ParserSettings> getDefaultValidators() {
        Map<String, ParserSettings> validators = new HashMap<>();

        validators.put("display-name", new ParserSettings()
                .addKey("username")
                .addKey("name")
                .length(4, 32)
                .addRegex(new RegexComponent()
                        .setAction(REJECT)
                        .setLine("[A-Z,a-z,0-9]*"))
        );


        validators.put("chat-messages", new ParserSettings()
                .addKey("message")
                .length(1, 76)
                .addRegex(new RegexComponent()
                        .setAction(REPLACE)
                        .setLine("(f..(c|k))")
                        .setReplacement("*^$#!?"))
        );

        return validators;
    }
}
