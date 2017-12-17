package com.codingchili.realm.model;

import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.model.entity.PlayerEntity;

import java.util.Collection;

/**
 * @author Robin Duda
 */
public class CharacterList {
    private RealmSettings realm;
    private Collection<PlayerEntity> characters;

    public CharacterList(RealmSettings realm, Collection<PlayerEntity> characters) {
        this.realm = realm.removeAuthentication();
        this.characters = characters;
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public void setRealm(RealmSettings realm) {
        this.realm = realm;
    }

    public Collection<PlayerEntity> getCharacters() {
        return characters;
    }

    public void setCharacters(Collection<PlayerEntity> characters) {
        this.characters = characters;
    }
}
