package com.codingchili.core.context;

import static com.codingchili.core.configuration.CoreStrings.EXT_JSON;
import static com.codingchili.core.configuration.CoreStrings.NODE_EXT;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.security.RemoteIdentity;

/**
 * @author Robin Duda
 *         <p>
 *         Simple service context.
 */
public class SimpleServiceContext extends ServiceContext {
    private String path;

    /**
     * @param context core instance to branch on.
     * @param node    the name and address of the node.
     */
    public SimpleServiceContext(CoreContext context, String node) {
        super(context);
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
