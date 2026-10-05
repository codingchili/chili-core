package com.codingchili.core.status;

import io.vertx.core.Future;

/**
 * A check of something that the application depends on to serve requests, for example a storage or another service.
 * Checks are executed whenever the readiness of the application is requested, they should be cheap, and must not block
 * the event loop. See {@link StatusService#check(String, ReadinessCheck)}.
 */
@FunctionalInterface
public interface ReadinessCheck {

    /**
     * @return a future that succeeds when the dependency is ready, and fails when it is not. The message of the
     * failure is included in the status report: it should not contain anything that is sensitive.
     */
    Future<Void> check();
}
