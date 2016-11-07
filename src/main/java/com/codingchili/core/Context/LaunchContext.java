package com.codingchili.core.Context;

import io.vertx.core.*;

import java.util.List;

import com.codingchili.core.Configuration.System.LauncherSettings;
import com.codingchili.core.Exception.BlockNotConfiguredException;
import com.codingchili.core.Exception.RemoteBlockNotConfiguredException;
import com.codingchili.core.Files.Configurations;
import com.codingchili.core.Protocol.ClusterNode;
import com.codingchili.core.Security.RemoteIdentity;

import static com.codingchili.core.Configuration.Strings.ID_DEFAULT;

/**
 * @author Robin Duda
 */
public class LaunchContext extends SystemContext {
    private Vertx vertx;
    private String[] args;


    public LaunchContext(String[] args) {
        super(Vertx.vertx());
        this.args = args;
    }

    public LaunchContext(Vertx vertx) {
        super(vertx);
        this.vertx = vertx;
    }

    public void deploy(String node, Handler<AsyncResult<String>> handler) {
        vertx.deployVerticle(node, handler);
    }

    @Override
    public RemoteIdentity identity() {
        return new RemoteIdentity("launcher", "local");
    }

    public void deploy(ClusterNode node, Handler<AsyncResult<String>> handler) {
        vertx.deployVerticle(node, handler);
    }

    public LauncherSettings settings() {
        return Configurations.launcher();
    }

    private boolean isRemoteBlock(String remote) {
        return settings().getHosts().containsKey(remote);
    }

    private boolean isBlockConfigured(String block) {
        return settings().getBlocks().containsKey(block);
    }

    private List<String> getBlockForRemote(String remote) throws RemoteBlockNotConfiguredException, BlockNotConfiguredException {
        String block = settings().getHosts().get(remote);

        if (isBlockConfigured(block)) {
            return getBlock(block);
        } else {
            throw new RemoteBlockNotConfiguredException(remote, block);
        }
    }

    private List<String> getBlock(String block) throws BlockNotConfiguredException {
        if (isBlockConfigured(block)) {
            return settings().getBlocks().get(block);
        } else if (block == null) {
            if (isBlockConfigured(ID_DEFAULT)) {
                return settings().getBlocks().get(ID_DEFAULT);
            } else {
                throw new BlockNotConfiguredException(ID_DEFAULT);
            }
        } else {
            throw new BlockNotConfiguredException(block);
        }
    }

    public List<String> block(String block) throws RemoteBlockNotConfiguredException, BlockNotConfiguredException {
        if (isRemoteBlock(block)) {
            return getBlockForRemote(block);
        } else {
            return getBlock(block);
        }
    }

    public String[] args() {
        return args;
    }
}
