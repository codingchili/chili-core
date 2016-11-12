package com.codingchili.core.Context;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.Exception.BlockNotConfiguredException;
import com.codingchili.core.Exception.RemoteBlockNotConfiguredException;

import static com.codingchili.core.Context.LaunchContextMock.*;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class LaunchContextTest {
    private LaunchContext context;

    @Before
    public void setUp() {
        this.context = new LaunchContextMock(new String[]{});
    }

    @Test
    public void testGetBlock() throws RemoteBlockNotConfiguredException, BlockNotConfiguredException {
        Assert.assertTrue(context.block(BLOCK_1).contains(SERVICE_1));
        Assert.assertTrue(context.block(BLOCK_2).contains(SERVICE_2));
    }

    @Test
    public void testGetHostBlock() throws RemoteBlockNotConfiguredException, BlockNotConfiguredException {
        Assert.assertTrue(context.block(HOST_1).contains(SERVICE_1));
        Assert.assertTrue(context.block(HOST_2).contains(SERVICE_1));
    }

    @Test
    public void testGetMissingBlock() throws RemoteBlockNotConfiguredException {
        try {
            Assert.assertTrue(context.block(BLOCK_NULL).isEmpty());
        } catch (BlockNotConfiguredException e) {
            Assert.assertTrue(e.getMessage().contains(BLOCK_NULL));
        }
    }

    @Test
    public void testGetMissingHost() throws BlockNotConfiguredException {
        try {
            context.block(HOST_3).contains(SERVICE_1);
        } catch (RemoteBlockNotConfiguredException e) {
            Assert.assertTrue(e.getMessage().contains(HOST_3));
            Assert.assertTrue(e.getMessage().contains(BLOCK_NULL));
        }
    }
}
