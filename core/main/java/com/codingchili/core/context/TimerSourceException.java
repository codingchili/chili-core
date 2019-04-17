package com.codingchili.core.context;

/**
 * Thrown on timer source failure.
 */
public class TimerSourceException extends CoreRuntimeException {

    /**
     * @param description the description for the error.
     */
    public TimerSourceException(String description) {
        super(description);
    }
}
