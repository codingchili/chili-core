package com.codingchili.core.security;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import com.codingchili.core.configuration.system.ValidatorSettings;
import com.codingchili.core.protocol.exception.RequestValidationException;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.security.RegexAction.*;

/**
 * Tests the validation mechanism.
 */
@RunWith(VertxUnitRunner.class)
public class ValidatorTest {
    private static final String NESTED = "nested";
    private static final String KEY = "key";
    private static Validator validator;
    private static Set<ValidatorSettings> settings;

    @Before
    public void setUp() {
        settings = getDefaultValidators();
        validator = new Validator();
        settings.forEach(validator::add);
    }

    @Test
    public void testMessageIsFiltered() throws RequestValidationException {
        Assert.assertEquals("test " +
            validator.get("chat-messages").getRegex().get(0).getSubstitution() +
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
        test.assertFalse(plaintext("abc-102"));
        test.assertTrue(plaintext(1000));
        test.assertTrue(plaintext(1000L));
        test.assertTrue(plaintext(Byte.valueOf("0")));

        String[] invalid = {"?", "_", "%", "^", ".", "*", "-", "!", ";", ",", "$", "&", "<"};

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
            test.assertTrue(plaintext(Validator.toPlainText(text)));
        }
    }

    @Test
    public void testNestedAttributeOk() throws RequestValidationException {
        validator.validate(getNestedObject("ok"));
    }

    @Test
    public void testNestedAttributeFail(TestContext test) {
        try {
            validator.validate(getNestedObject("too-long-should-fail"));
            test.fail("validation did not fail for nested object.");
        } catch (RequestValidationException ignored) {
        }
    }

    @Test
    public void testAttributeInArray(TestContext test) {
        JsonObject json = new JsonObject()
            .put("list", new JsonArray()
                .add(new JsonObject().put(ID_USERNAME, "invalid_username//#!!;"))
            );
        try {
            validator.validate(json);
            test.fail("failed to run validation on values in array.");
        } catch (RequestValidationException e) {
        }
    }

    @Test
    public void testSimpleElementInArray(TestContext test) {
        JsonObject json = new JsonObject()
            .put(ID_USERNAME, new JsonArray()
                .add("invalid_username//#!!;")
            );
        try {
            validator.validate(json);
            test.fail("failed to run validation on values in array.");
        } catch (RequestValidationException e) {
        }
    }

    @Test
    public void testArrayWithinArray(TestContext test) {
        JsonObject json = new JsonObject()
            .put(ID_NAME,
                new JsonArray()
                    .add(new JsonArray()
                        .add("invalid_username//#!!;")));
        try {
            validator.validate(json);
            test.fail("failed to run validation on values in array.");
        } catch (RequestValidationException e) {
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

    private Set<ValidatorSettings> getDefaultValidators() {
        Set<ValidatorSettings> settings = new HashSet<>();

        settings.add(new ValidatorSettings("user-name")
            .addKeys(ID_USERNAME, ID_NAME)
            .length(4, 32)
            .addRegex(ACCEPT, "[A-Z,a-z,0-9]*")
        );


        settings.add(new ValidatorSettings("chat-messages")
            .addKey(ID_MESSAGE)
            .length(1, 76)
            .addRegex(SUBSTITUTE, "(f..(c|k))", "*^$#!?")
        );

        settings.add(new ValidatorSettings("reject")
            .addRegex(REJECT, ("[!]"))
        );

        settings.add(new ValidatorSettings("nested-length")
            .addKey(KEY)
            .length(2, 4)
        );

        return settings;
    }
}
