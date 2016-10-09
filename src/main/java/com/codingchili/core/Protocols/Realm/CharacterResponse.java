package com.codingchili.core.Protocols.Realm;

import com.codingchili.core.Configuration.Strings;
import com.codingchili.core.Protocols.Header;
import com.codingchili.core.Realm.Instance.Model.PlayerCharacter;

/**
 * @author Robin Duda
 *         Response containing a character from the authentication server.
 */
public class CharacterResponse {
    public static final String ACTION = Strings.REALM_CHARACTER_RESPONSE;
    private Header header;
    private PlayerCharacter character;
    private boolean success;

    private CharacterResponse() {
        this.header = new Header(ACTION);
    }

    public CharacterResponse(PlayerCharacter character) {
        this();
        this.character = character;
        this.success = true;
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
