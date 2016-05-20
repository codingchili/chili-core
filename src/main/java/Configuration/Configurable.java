package Configuration;

/**
 * @author Robin Duda
 *         Used to write changes to configuration files.
 */
interface Configurable {
    /**
     * Get the path of a loaded configuration file.
     * @return the directory path to the configuration file.
     */
    String getPath();

    /**
     * Get the name of the configuration file.
     * @return name as string with extension.
     */
    String getName();
}
