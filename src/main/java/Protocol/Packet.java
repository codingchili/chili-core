package Protocol;

/**
 * @author Robin Duda
 *         packet model contains a header only, used for partial unpacking.
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