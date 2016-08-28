package Authentication.Model;

import Realm.Model.PlayerCharacter;

import java.util.HashMap;

public class Account {
    private HashMap<String, HashMap<String, PlayerCharacter>> characters = new HashMap<>();
    private String username;
    private String password;
    private String email;

    public Account() {
    }

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Account(Account account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.characters = account.getCharacters();
    }

    public Account(AccountMapping account) {
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

    public String getUsername() {
        return username;
    }

    public Account setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Account setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }

    public void addCharacter(String realmName, PlayerCharacter playerCharacter) {
        characters.putIfAbsent(realmName, new HashMap<>());
        characters.get(realmName).put(playerCharacter.getName(), playerCharacter);
    }
}
