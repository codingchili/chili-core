package Authentication.Model;

import Game.Model.Character;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Holds account data for an user used to communicate with the view.
 */
public class Account {
    private ArrayList<Character> characters = new ArrayList<>();
    private String username;
    private String password;
    private String email;

    public Account() {
    }

    public Account(AccountMapping account) {
        this.username = account.getUsername();
        this.email = account.getEmail();
    }

    public ArrayList<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(ArrayList<Character> characters) {
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
}
