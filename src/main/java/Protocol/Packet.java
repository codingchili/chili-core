package Protocol;

/**
 * Created by Robin on 2016-04-27.
 */
public class Packet {
    private Header header;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getAction() {
        return header.getAction();
    }
}