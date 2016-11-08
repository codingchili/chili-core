package com.codingchili.services.Realm.Model;

import java.util.Collection;
import java.util.Map;

import com.codingchili.services.Realm.Configuration.RealmSettings;
import com.codingchili.services.Realm.Instance.Model.PlayerCharacter;

/**
 * @author Robin Duda
 */
public class CharacterList {
    private RealmSettings realm;
    private Collection<PlayerCharacter> characters;

    public CharacterList(RealmSettings realm, Map<String, PlayerCharacter> characters) {
        this.realm = realm.removeAuthentication();
        this.characters = characters.values();
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
