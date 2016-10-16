package com.codingchili.core.Protocols.Authentication;

import com.codingchili.core.Realm.Configuration.RealmSettings;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;

import java.util.Collection;
import java.util.Map;

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
