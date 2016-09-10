package Authentication;

import Authentication.Model.*;
import Realm.Model.PlayerCharacter;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 *         mock implementation of an account store used for testing.
 */
class AccountStoreMock implements AsyncAccountStore {
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String REALM_NAME = "realmName.name";
    private static final String CHARACTER_NAME = "character";
    private static final String CHARACTER_NAME_DELETED = "character-deleted";
    private HashMap<String, Account> accounts = new HashMap<>();

    AccountStoreMock() {
        Account account = new Account()
                .setPassword(PASSWORD)
                .setUsername(USERNAME);

        account.addCharacter(REALM_NAME, new PlayerCharacter().setName(CHARACTER_NAME));
        account.addCharacter(REALM_NAME, new PlayerCharacter().setName(CHARACTER_NAME_DELETED));

        accounts.put(USERNAME, account);
    }

    @Override
    public void find(Future<Account> future, String username) {
        if (accounts.containsKey(username))
            future.complete(accounts.get(username));
        else
            future.fail(new AccountMissingException());
    }

    @Override
    public void authenticate(Future<Account> future, Account account) {
        if (accounts.containsKey(account.getUsername())) {
            Account authenticated = accounts.get(account.getUsername());

            if (authenticated.getPassword().equals(account.getPassword()))
                future.complete(account);
            else
                future.fail(new AccountPasswordException());
        } else
            future.fail(new AccountMissingException());
    }

    @Override
    public void register(Future<Account> future, Account account) {
        if (accounts.containsKey(account.getUsername())) {
            future.fail(new AccountExistsException());
        } else {
            accounts.put(account.getUsername(), account);
            future.complete(account);
        }
    }

    @Override
    public void upsertCharacter(Future future, String realm, String username, PlayerCharacter character) {
        if (accounts.containsKey(username)) {
            Account account = accounts.get(username);
            account.getCharacters().get(realm).put(character.getName(), character);
            future.complete();
        }
    }

    @Override
    public void findCharacters(Future<ArrayList<PlayerCharacter>> future, String realm, String username) {
        if (accounts.containsKey(username)) {
            ArrayList<PlayerCharacter> characters = new ArrayList<>();
            HashMap<String, HashMap<String, PlayerCharacter>> list = accounts.get(username).getCharacters();

            if (list.containsKey(realm))
                for (String key : list.get(realm).keySet()) {
                    characters.add(list.get(realm).get(key));
                }

            future.complete(characters);
        } else
            future.fail(new AccountMissingException());
    }

    @Override
    public void findCharacter(Future<PlayerCharacter> future, String realm, String username, String name) {
        if (accounts.containsKey(username)) {

            if (accounts.get(username).getCharacters().containsKey(realm)) {

                if (accounts.get(username).getCharacters().get(realm).containsKey(name)) {
                    PlayerCharacter character = accounts.get(username).getCharacters().get(realm).get(name);
                    future.complete(character);
                } else
                    future.fail(new CharacterMissingException());
            } else
                future.fail(new CharacterMissingException());
        } else
            future.fail(new AccountMissingException());
    }

    @Override
    public void removeCharacter(Future future, String realm, String username, String character) {
        HashMap<String, HashMap<String, PlayerCharacter>> characters = accounts.get(username).getCharacters();

        if (characters.containsKey(realm) && characters.get(realm).containsKey(character)) {
            characters.get(realm).remove(character);
            future.complete();
        } else
            future.fail(new CharacterMissingException());
    }
}
