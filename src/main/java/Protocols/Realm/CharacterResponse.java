package Protocols.Realm;

import Configuration.Strings;
import Realm.Model.PlayerCharacter;
import Protocols.Header;

/**
 * @author Robin Duda
 *         Response containing a character from the authentication server.
 */
public class CharacterResponse {
    public static final String ACTION = Strings.REALM_CHARACTER_RESPONSE;
    private Header header;
    private PlayerCharacter character;
    private String sender;
    private boolean success;

    private CharacterResponse() {
        this.header = new Header(ACTION);
    }

    public CharacterResponse(PlayerCharacter character, String sender) {
        this();
        this.character = character;
        this.sender = sender;
        this.success = true;
    }

    public String getConnection() {
        return sender;
    }

    public void setConnection(String sender) {
        this.sender = sender;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public PlayerCharacter getCharacter() {
        return character;
    }

    public void setCharacter(PlayerCharacter character) {
        this.character = character;
    }

    public boolean isSuccess() {
        return success;
    }

    public CharacterResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }
}
