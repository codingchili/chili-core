package com.codingchili.core.context;

import io.vertx.core.Vertx;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.RemoteIdentity;

import static com.codingchili.core.configuration.CoreStrings.EXT_JSON;
import static com.codingchili.core.configuration.CoreStrings.NODE_EXT;

/**
 * @author Robin Duda
 */
public class SimpleServiceContext extends ServiceContext {
    private String path;

    public SimpleServiceContext(Vertx vertx, String node) {
        super(vertx);
        this.path = CoreStrings.DIR_SERVICES + CoreStrings.remove(node, NODE_EXT) + EXT_JSON;
        Configurations.put(new ServiceConfigurable(path)
                .setIdentity(new RemoteIdentity().setNode(node)));
    }

    /**
     * @return the configuration associated with the current context.
     */
    public ServiceConfigurable service() {
        return Configurations.get(path, ServiceConfigurable.class);
    }


}
