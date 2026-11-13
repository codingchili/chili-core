package com.codingchili.core.protocol;

import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import io.vertx.core.json.DecodeException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * What a client can cause at will is a refusal (logged at DEBUG), anything else a fault (logged as an error).
 */
public class ProtocolRefusalTest {
    @Rule
    public Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

    @Test
    public void clientMistakesAreRefusals() {
        Assert.assertTrue(Protocol.refusal(new AuthorizationRequiredException()));
        Assert.assertTrue(Protocol.refusal(new HandlerMissingException("nope")));
        Assert.assertTrue(Protocol.refusal(new DecodeException("not json")));
    }

    @Test
    public void faultsAreNot() {
        Assert.assertFalse(Protocol.refusal(new NullPointerException()));
        Assert.assertFalse(Protocol.refusal(new ClassCastException()));
        Assert.assertFalse(Protocol.refusal(new IllegalStateException()));
    }
}
