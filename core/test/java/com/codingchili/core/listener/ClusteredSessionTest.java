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
    private static String source = "source";
    private static String connection = "connection";
    private CoreContext core;
    private ClusteredSession session;
    private SessionFactory<ClusteredSession> sessionFactory;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();
        core = new SystemContext();
        sessionFactory = new SessionFactoryMock(core);

        sessionFactory.create(source, connection).setHandler(created -> {
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
        test.assertEquals(source, session.source());
        test.assertEquals(connection, session.connection());

        sessionFactory.isActive(session).setHandler(active -> {
            test.assertTrue(active.result());
        });
    }

    @Test
    public void testUpdate(TestContext test) {
        session.data().put("pass", true);
        session.update().setHandler(updated -> {
            test.assertTrue(updated.succeeded());
            test.assertTrue(SessionFactoryMock.sessions.get(session.id()).data().containsKey("pass"));
        });
    }

    @Test
    public void testWriteToSource(TestContext test) {
        Async async = test.async(2);

        core.bus().consumer(source, msg -> {
            test.assertEquals(source, msg.headers().get(source));
            test.assertEquals(connection, msg.headers().get(connection));
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

        session.data().put(text, text);
        JsonObject json = Serializer.json(session);
        Session session2 = Serializer.unpack(json, ClusteredSession.class);
        test.assertEquals(session, session2);
        test.assertEquals(text, session2.data().getString(text));
        test.assertNotNull(session2.source());
        test.assertNotNull(session2.connection());
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