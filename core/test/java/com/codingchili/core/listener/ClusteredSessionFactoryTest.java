package com.codingchili.core.listener;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the clustered session store.
 */
@RunWith(VertxUnitRunner.class)
public class ClusteredSessionFactoryTest {
    private String home = "home";
    private String connection = "connection";
    private CoreContext core = new SystemContext();
    private SessionFactory<ClusteredSession> factory;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        ClusteredSessionFactory.get(core).setHandler(get -> {
            factory = get.result();
            async.complete();
        });
    }

    @After
    public void tearDown(TestContext test) {
        core.close(test.asyncAssertSuccess());
    }

    @Test
    public void testCreateSession(TestContext test) {
        Async async = test.async();

        factory.create(home, connection).setHandler(done -> {
            test.assertTrue(done.succeeded());

            factory.isActive(done.result()).setHandler(active -> {
                test.assertTrue(active.result());
                async.complete();
            });
        });
    }

    @Test
    public void testDestroySession(TestContext test) {
        Async async = test.async();

        factory.create(home, connection).setHandler(done -> {
            ClusteredSession session = done.result();
            test.assertTrue(done.succeeded());

            factory.destroy(session).setHandler(destroyed -> {
                test.assertTrue(destroyed.succeeded());

                factory.isActive(session).setHandler(active -> {
                    test.assertFalse(active.result());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testUpdateSession(TestContext test) {
        Async async = test.async();

        factory.create(home, connection).setHandler(done -> {
            Session session = done.result();
            test.assertTrue(done.succeeded());

            session.asJson().put("meow", true);
            session.update().setHandler(updated -> {
                test.assertTrue(updated.succeeded());

                factory.query("data.meow").equalTo(true).execute(query -> {
                    test.assertTrue(query.succeeded());
                    test.assertEquals(1, query.result().size());
                    async.complete();
                });
            });
        });
    }
}