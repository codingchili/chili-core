package com.codingchili.core.context;

import com.codingchili.core.configuration.*;
import com.codingchili.core.files.*;
import com.codingchili.core.security.*;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Simple service context.
 */
public class SimpleServiceContext extends ServiceContext {
    private String path;

    /**
     * Creates a new simple service context without a version.
     *
     * @param context core instance to branch on.
     * @param node    the name and address of the node.
     */
    public SimpleServiceContext(CoreContext context, String node) {
        this(context, node, null);
    }

    /**
     * Creates a new simple service context with a version.
     *
     * @param context core instance to branch on.
     * @param node    the name and address of the node.
     * @param version the version of the handler.
     */
    public SimpleServiceContext(CoreContext context, String node, String version) {
        super(context, node);
        this.path = CoreStrings.DIR_SERVICES + CoreStrings.remove(node, NODE_EXT) + EXT_JSON;
        Configurations.put(new ServiceConfigurable(path)
                .setIdentity(new RemoteIdentity()
                        .setNode(node)
                        .setVersion(version)));
    }

    /**
     * @return the configuration associated with the current context.
     */
    public ServiceConfigurable service() {
        return Configurations.get(path, ServiceConfigurable.class);
    }
}
