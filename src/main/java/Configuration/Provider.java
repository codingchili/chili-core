package Configuration;

import Logging.Model.Logger;
import io.vertx.core.Vertx;

/**
 * @author Robin Duda
 */
public interface Provider {

    Logger getLogger();

    ConfigurationLoader getConfig();

    Vertx getVertx();
}
