package Protocols.Realm;

import Configuration.Strings;
import Protocols.Header;
import Protocols.Authorization.Token;

/**
 * @author Robin Duda
 *         Character download request from game->authentication server.
 */
public class CharacterRequest {
    public final static String ACTION = Strings.REALM_CHARACTER_REQUEST;
    private Header header;
    private Token token;
    private String name;
    private String connection;
    private boolean success;

    public CharacterRequest() {
        this.header = new Header(ACTION);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public boolean isSuccess() {
        return success;
    }

    public CharacterRequest setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getAccount() {
        return token.getDomain();
    }
}
