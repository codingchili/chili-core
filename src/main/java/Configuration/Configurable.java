package Configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Robin Duda
 *         Used to write changes to configuration files.
 */
public interface Configurable {
    /**
     * Get the path of a loaded configuration file.
     *
     * @return the directory path to the configuration file.
     */
    @JsonIgnore
    String getPath();

    /**
     * Get the name of the configuration file.
     *
     * @return name as string with extension.
     */
    @JsonIgnore
    String getName();


    RemoteAuthentication getLogserver();
}
