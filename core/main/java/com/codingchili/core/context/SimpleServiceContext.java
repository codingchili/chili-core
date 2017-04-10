package com.codingchili.core.context;

import com.codingchili.core.configuration.*;
import com.codingchili.core.files.*;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Simple service context.
 */
public class SimpleServiceContext extends ServiceContext {
    private String path;

    /**
     * Creates a new simple service context.
     *
     * @param context core instance to branch on.
     * @param address the name and identity of the node.
     */
    public SimpleServiceContext(CoreContext context, String address) {
        super(context);
        path = CoreStrings.DIR_SERVICES + CoreStrings.remove(address, NODE_EXT) + EXT_JSON;
        Configurations.put(new ServiceConfigurable(path).setNode(address));
    }

    /**
     * @return the configuration associated with the current context.
     */
    public ServiceConfigurable service() {
        return Configurations.get(path, ServiceConfigurable.class);
    }
}
