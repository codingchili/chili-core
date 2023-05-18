package com.codingchili.core.listener;

import com.codingchili.core.storage.AttributeRegistry;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * Tests for the clustered session store.
 */
@RunWith(VertxUnitRunner.class)
public class ClusteredSessionFactoryTest {
    private final String home = "home";
    private final String connection = "connection";
    private final CoreContext core = new SystemContext();
    private SessionFactory<ClusteredSession> factory;

    @Before
    public void setUp(TestContext test) {
        Async async = test.async();

        ClusteredSessionFactory.get(core).onComplete(get -> {
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

        factory.create(home, connection).onComplete(done -> {
            test.assertTrue(done.succeeded());

            factory.isActive(done.result()).onComplete(active -> {
                test.assertTrue(active.result());
                async.complete();
            });
        });
    }

    @Test
    public void testDestroySession(TestContext test) {
        Async async = test.async();

        factory.create(home, connection).onComplete(done -> {
            ClusteredSession session = done.result();
            test.assertTrue(done.succeeded());

            factory.destroy(session).onComplete(destroyed -> {
                test.assertTrue(destroyed.succeeded());

                factory.isActive(session).onComplete(active -> {
                    test.assertFalse(active.result());
                    async.complete();
                });
            });
        });
    }

    @Test
    public void testUpdateSession(TestContext test) {
        Async async = test.async();

        factory.create(home, connection).onComplete(done -> {
            Session session = done.result();
            test.assertTrue(done.succeeded());

            AttributeRegistry.register(db -> {
                db.single(s -> s.asJson().getBoolean("meow"), Boolean.class, "data.meow");
            }, ClusteredSession.class);

            session.asJson().put("meow", true);
            session.update().onComplete(updated -> {

                factory.query("data.meow").equalTo(true).execute(query -> {
                    if (query.failed()) {
                        test.fail(query.cause());
                    }
                    test.assertEquals(1, query.result().size());
                    async.complete();
                });
            });
        });
    }
}