package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.codingchili.core.configuration.CoreStrings.PROTOCOL_STATUS;
import static com.codingchili.core.protocol.ResponseStatus.ACCEPTED;

/**
 * Tests for the cluster writer helper.
 */
@RunWith(VertxUnitRunner.class)
public class ClusterHelperTest {
    private static final String address = ClusterHelperTest.class.getName();
    private CoreContext core;

    @Before
    public void setUp(TestContext test) {
        core = new SystemContext();
    }

    @After
    public void tearDown(TestContext test) {
        core.close(test.asyncAssertSuccess());
    }

    @Test
    public void testWriteBuffer(TestContext test) {
        Buffer buffer = Buffer.buffer("test");
        sendAndReply(buffer, (res) -> {
            test.assertEquals(buffer, res);
        }, test.async());
    }

    @Test
    public void testWriteCollection(TestContext test) {
        List<POJO> list = new ArrayList<>();
        list.add(new POJO().setStatus("test"));
        sendAndReply(list,  res -> {
            test.assertEquals(Serializer.json(list).put(PROTOCOL_STATUS, ACCEPTED), res);
        }, test.async());
    }

    @Test
    public void testWriteJsonObject(TestContext test) {
        JsonObject json = new JsonObject().put("testing", true);
        sendAndReply(json, res -> {
            test.assertEquals(json, res);
        }, test.async());
    }

    @Test
    public void testWritePOJO(TestContext test) {
        POJO pojo = new POJO().setStatus("accepted");

        sendAndReply(pojo, res -> {
            test.assertEquals(Serializer.json(pojo), res);
        }, test.async());
    }

    private <T> void sendAndReply(T object, Consumer<Object> assertion, Async async) {
        core.bus().consumer(address, msg -> {
            ClusterHelper.reply(msg, object);
        });

        core.bus().send(address, new JsonObject(), msg -> {
            assertion.accept(msg.result().body());
            async.complete();
        });
    }

    private class POJO {
        private String status;


        public String getStatus() {
            return status;
        }

        public POJO setStatus(String status) {
            this.status = status;
            return this;
        }
    }
}