package com.codingchili.core.protocol.exception;

import com.codingchili.core.context.CoreRuntimeException;

import static com.codingchili.core.configuration.CoreStrings.getDeserializePayloadException;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when an attempt has been made to deserialize an invalid payload.
 */
public class SerializerPayloadException extends CoreRuntimeException {

    public SerializerPayloadException(String message, Class clazz) {
        super(getDeserializePayloadException(message, clazz));
    }
}
