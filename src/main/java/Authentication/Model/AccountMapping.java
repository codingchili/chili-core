package Authentication.Model;

import Realm.Model.PlayerCharacter;

import java.util.HashMap;

/**
 * @author Robin Duda
 *         Database mapping not shared outside storage.
 */
class AccountMapping {
    private HashMap<String, HashMap<String, PlayerCharacter>> characters = new HashMap<>();
    private String username;
    private String email;
    private String salt;
    private String hash;

    public AccountMapping() {
    }

    public AccountMapping(Account account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.characters = account.getCharacters();
    }

    public HashMap<String, HashMap<String, PlayerCharacter>> getCharacters() {
        return characters;
    }

    public void setCharacters(HashMap<String, HashMap<String, PlayerCharacter>> characters) {
        this.characters = characters;
    }

    public String getSalt() {
        return salt;
    }

    public AccountMapping setSalt(String salt) {
        this.salt = salt;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AccountMapping setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public AccountMapping setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public AccountMapping setEmail(String email) {
        this.email = email;
        return this;
    }
}
