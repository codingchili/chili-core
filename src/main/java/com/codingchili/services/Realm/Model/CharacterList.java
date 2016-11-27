package com.codingchili.services.realm.model;

import java.util.*;

import com.codingchili.services.realm.configuration.RealmSettings;
import com.codingchili.services.realm.instance.model.PlayerCharacter;

/**
 * @author Robin Duda
 */
public class CharacterList {
    private RealmSettings realm;
    private Collection<PlayerCharacter> characters;

    public CharacterList(RealmSettings realm, Collection<PlayerCharacter> characters) {
        this.realm = realm.removeAuthentication();
        this.characters = characters;
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public void setRealm(RealmSettings realm) {
        this.realm = realm;
    }

    public Collection<PlayerCharacter> getCharacters() {
        return characters;
    }

    public void setCharacters(Collection<PlayerCharacter> characters) {
        this.characters = characters;
    }
}
