package Protocols.Authentication;

import Authentication.Model.Account;
import Protocols.Util.Token;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Robin Duda
 *         an authentication message from the server to the view.
 */
public class ClientAuthentication {
    private ArrayList<RealmMetaData> realms = new ArrayList<>();
    private HashMap<String, Integer> favourites = new HashMap<>();
    private Token token;
    private Account account;
    private boolean registered;

    public ClientAuthentication() {
    }

    public ClientAuthentication(Account account, Token token, boolean registered, ArrayList<RealmMetaData> realms) {

        // Extract the names of the realms where the account has characters.
        for (String realm : account.getCharacters().keySet()) {
            if (!account.getCharacters().get(realm).isEmpty())
                favourites.put(realm, account.getCharacters().get(realm).size());
        }

        //Remove the list of characters as they are not needed yet.
        account.setCharacters(null);

        this.account = account;
        this.token = token;
        this.registered = registered;
        this.realms = realms;
    }

    public HashMap<String, Integer> getFavourites() {
        return favourites;
    }

    public void setFavourites(HashMap<String, Integer> favourites) {
        this.favourites = favourites;
    }

    public ArrayList<RealmMetaData> getRealms() {
        return realms;
    }

    public void setRealms(ArrayList<RealmMetaData> realms) {
        this.realms = realms;
    }

    public Token getToken() {
        return token;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
