package com.codingchili.core.context;

/**
 * @author Robin Duda
 * <p>
 * Indicates that a deployable unit is capable of providing deployment options.
 */
public interface DeploymentAware {

    /**
     * @return number of instances to deploiy of the given deployable..
     */
    int instances();
}
