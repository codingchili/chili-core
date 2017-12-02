package com.codingchili.realm.instance.model;

import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RoleMap;

/**
 * @author Robin Duda
 */
public class EventProtocol<Event> extends Protocol<Event> {
    private Integer id;

    public EventProtocol(Entity entity) {
        this.id = entity.getId();
        setRole(RoleMap.get(RoleMap.PUBLIC));
        annotated(entity);
    }

    public Integer getId() {
        return id;
    }
}
