package Protocol.Game;

import Game.Model.PlayerCharacter;
import Protocol.Header;

/**
 * @author Robin Duda
 *         Response containing a character from the authentication server.
 */
public class CharacterResponse {
    public static final String ACTION = "character.response";
    private Header header;
    private PlayerCharacter character;
    private String connection;
    private boolean success;

    public CharacterResponse() {
        this.header = new Header(ACTION);
    }

    public CharacterResponse(PlayerCharacter character, CharacterRequest request) {
        this();
        this.character = character;
        this.connection = request.getConnection();
        this.success = true;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
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
