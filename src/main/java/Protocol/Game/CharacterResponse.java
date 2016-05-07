package Protocol.Game;

import Game.Model.PlayerCharacter;
import Protocol.Header;

/**
 * Created by Robin on 2016-05-08.
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
