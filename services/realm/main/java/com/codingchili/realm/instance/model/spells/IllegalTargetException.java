package com.codingchili.realm.instance.model.spells;

import com.codingchili.core.context.CoreRuntimeException;

/**
 * @author Robin Duda
 */
public class IllegalTargetException extends CoreRuntimeException {

    public IllegalTargetException(String name) {
        super(String.format("Illegal target for spell '%s', no target exists.", name));
    }

}
