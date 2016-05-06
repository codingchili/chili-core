package Authentication.Model;

import Game.Model.PlayerCharacter;
import io.vertx.core.Future;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         <p>
 *         Asynchronous account store.
 */
public interface AsyncAccountStore {
    /**
     * Finds an account in the store.
     *
     * @param username username of the account to find.
     */
    void find(Future<Account> future, String username);

    /**
     * Authenticates an user in the accountstore.
     *
     * @param account unauthenticated account containing username and password.
     */
    void authenticate(Future<Account> future, Account account);

    /**
     * Registers a new account in the store.
     *
     * @param account contains account data to be created.
     */
    void register(Future<Account> future, Account account);

    /**
     * Adds a character to an account.
     *
     * @param realm     the realm which the character is added to.
     * @param account   the name of the account the character is added to.
     * @param character the character to be added.
     */
    void addCharacter(Future<Void> future, String realm, String account, PlayerCharacter character);

    /**
     *
     * @param future
     * @param realm
     * @param accountName
     */
    void findCharacters(Future<ArrayList<PlayerCharacter>> future, String realm, String accountName);
}
