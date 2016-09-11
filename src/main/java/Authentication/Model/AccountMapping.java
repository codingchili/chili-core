package Authentication.Model;

import Realm.Model.PlayerCharacter;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Robin Duda
 *         Database mapping not shared outside storage.
 */
class AccountMapping implements Serializable {
    private HashMap<String, HashMap<String, PlayerCharacter>> realms = new HashMap<>();
    private String username;
    private String email;
    private String salt;
    private String hash;

    public AccountMapping() {
    }

    public AccountMapping(Account account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.realms = account.getCharacters();
    }

    public HashMap<String, HashMap<String, PlayerCharacter>> getRealms() {
        return realms;
    }

    public void setRealms(HashMap<String, HashMap<String, PlayerCharacter>> realms) {
        this.realms = realms;
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
