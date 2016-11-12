package com.codingchili.core.Context;

/**
 * @author Robin Duda
 *
 * Provides a method to get a timer interval that may change
 * after retrieving it.
 */
@FunctionalInterface
public interface TimerSource {
    /**
     * @return Returns the interval length in MS.
     */
    int getMS();
}
