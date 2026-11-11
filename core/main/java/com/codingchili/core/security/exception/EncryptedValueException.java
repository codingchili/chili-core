package com.codingchili.core.security.exception;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * Thrown when an encrypted value ({@code enc:}) can not be opened, see
 * {@link com.codingchili.core.security.EncryptedValue}.
 */
public class EncryptedValueException extends CoreRuntimeException {
    private final Reason reason;

    /**
     * Why a value could not be opened.
     */
    public enum Reason {
        /**
         * not an encrypted value or not valid base64/layout.
         */
        FORMAT,
        /**
         * a version of the format this build does not read.
         */
        VERSION,
        /**
         * this node has no key (no key file).
         */
        NO_KEY,
        /**
         * the key of this node is not one of the recipients of the value.
         */
        NOT_RECIPIENT,
        /**
         * the value was changed after it was encrypted (or is corrupt).
         */
        TAMPERED
    }

    /**
     * @param reason  why the value could not be opened.
     * @param message the description.
     */
    public EncryptedValueException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    /**
     * @return why the value could not be opened.
     */
    public Reason getReason() {
        return reason;
    }
}
