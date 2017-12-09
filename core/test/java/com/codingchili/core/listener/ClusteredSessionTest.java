package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the clustered session, makes sure it calls the sessionfactory.
 */
@RunWith(VertxUnitRunner.class)
public class ClusteredSessionTest {
    private static String home = "[home uid]";
    private static String connectionId = "[connection uid]";
    private CoreContext core;
    private ClusteredSession session;
    private SessionFactory<ClusteredSession> sessionFactory;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        core = new SystemContext();
        sessionFactory = new SessionFactoryMock(core);

        sessionFactory.create(home, connectionId).setHandler(created -> {
            session = created.result();
            async.complete();
        });
    }

    @After
    public void tearDown(TestContext test) {
        core.close(test.asyncAssertSuccess());
    }

    @Test
    public void testCreate(TestContext test) {
        test.assertEquals(home, session.getHome());
        test.assertEquals(connectionId, session.getId());

        sessionFactory.isActive(session).setHandler(active -> {
            test.assertTrue(active.result());
        });
    }

    @Test
    public void testUpdate(TestContext test) {
        session.asJson().put("pass", true);
        session.update().setHandler(updated -> {
            test.assertTrue(updated.succeeded());
            test.assertTrue(SessionFactoryMock.sessions.get(session.getId()).asJson().containsKey("pass"));
        });
    }

    @Test
    public void testWriteToSource(TestContext test) {
        Async async = test.async(2);

        core.bus().consumer(home, msg -> {
            test.assertEquals(home, msg.headers().get(Session.HOME));
            test.assertEquals(connectionId, msg.headers().get(Session.ID));
            async.countDown();
        });

        session.write(new TestMessage().setText("first"));
        session.write(new TestMessage().setText("second"));
    }

    @Test
    public void testDestroy(TestContext test) {
        session.destroy().setHandler(destroyed -> {
            session.isActive().setHandler(active -> {
                test.assertFalse(active.result());
            });
        });
    }

    @Test
    public void testSerialization(TestContext test) {
        String text = "test";

        session.asJson().put(text, text);
        JsonObject json = Serializer.json(session);
        Session session2 = Serializer.unpack(json, ClusteredSession.class);
        test.assertEquals(session.asJson().encode(), session2.asJson().encode());
        test.assertEquals(text, session2.asJson().getString(text));
        test.assertNotNull(session2.getHome());
        test.assertNotNull(session2.getId());
    }

    private class TestMessage {
        private String text;

        public String getText() {
            return text;
        }

        public TestMessage setText(String text) {
            this.text = text;
            return this;
        }
    }
}