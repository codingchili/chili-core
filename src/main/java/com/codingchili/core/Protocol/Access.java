package com.codingchili.core.Protocol;

/**
 * @author Robin Duda
 *
 * Access levels; authorized requests may access both routes set to
 * PUBLIC and AUTHORIZED. If the request is not authorized then the
 * request is only permitted to access PUBLIC routes.
 */
public enum Access {
    PUBLIC, AUTHORIZED
}