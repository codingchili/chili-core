package com.codingchili.realm.model;

import com.codingchili.common.Strings;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.realm.instance.model.entity.PlayerCreature;

import java.util.Collection;

/**
 * @author Robin Duda
 */
public class CharacterList {
    private RealmSettings realm;
    private Collection<PlayerCreature> characters;

    public CharacterList(RealmSettings realm, Collection<PlayerCreature> characters) {
        this.realm = realm.removeAuthentication();
        this.characters = characters;
    }

    public String getRoute() {
        return Strings.CLIENT_CHARACTER_LIST;
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public void setRealm(RealmSettings realm) {
        this.realm = realm;
    }

    public Collection<PlayerCreature> getCharacters() {
        return characters;
    }

    public void setCharacters(Collection<PlayerCreature> characters) {
        this.characters = characters;
    }
}
