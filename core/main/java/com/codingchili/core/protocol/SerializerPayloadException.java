package com.codingchili.core.protocol;

import com.codingchili.core.context.CoreRuntimeException;

import static com.codingchili.core.configuration.CoreStrings.getDeserializePayloadException;

/**
 * @author Robin Duda
 *         <p>
 *         Throw when an attempt has been made to deserialize an invalid payload.
 */
public class SerializerPayloadException extends CoreRuntimeException {

    protected SerializerPayloadException() {
        super(getDeserializePayloadException());
    }
}
