package com.codingchili.core.Exception;

/**
 * @author Robin Duda
 *
 * Exceptions should extend this class to allow for catching all core-type exceptions.
 */
public class CoreException extends Exception {
    protected CoreException(String error) {
        super(error);
    }
}
