package com.codingchili.core;

import io.vertx.core.Future;

import com.codingchili.core.protocol.ClusterNode;

import static com.codingchili.core.LauncherIT.async;

/**
 * @author Robin Duda
 */
public class IsClusterNode extends ClusterNode {
    @Override
    public void start(Future<Void> future) {
        async.complete();
        future.complete();
    }
}