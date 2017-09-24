package com.codingchili.core.protocol;

import static com.codingchili.core.protocol.RoleMap.USER;

public class UserCopyRole implements RoleType {

    @Override
    public String getName() {
        return RoleMap.get(USER).getName() + ".copy";
    }

    @Override
    public int getLevel() {
        return RoleMap.get(USER).getLevel();
    }
}
