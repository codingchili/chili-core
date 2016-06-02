package Protocols.Authentication;

import Configuration.Gameserver.RealmSettings;
import Game.Model.PlayerCharacter;

import java.util.ArrayList;

/**
 * @author Robin Duda
 */
public class CharacterList {
    private RealmSettings realm;
    private ArrayList<PlayerCharacter> characters;

    public CharacterList(RealmSettings realm, ArrayList<PlayerCharacter> characters) {
        this.realm = realm.removeAuthentication();
        this.characters = characters;
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public void setRealm(RealmSettings realm) {
        this.realm = realm;
    }

    public ArrayList<PlayerCharacter> getCharacters() {
        return characters;
    }

    public void setCharacters(ArrayList<PlayerCharacter> characters) {
        this.characters = characters;
    }
}
