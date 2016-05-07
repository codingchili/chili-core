package Protocol.Game;

import Protocol.Header;

/**
 * Created by Robin on 2016-05-07.
 */
public class AuthenticationResult {
    public static final String ACTION = "authentication.result";
    private boolean success;
    private Header header;

    public AuthenticationResult() {
    }

    public AuthenticationResult(boolean result) {
        this.success = result;
        this.header = new Header(ACTION);
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
