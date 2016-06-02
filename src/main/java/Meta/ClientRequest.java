package Meta;

import Protocols.Request;
import io.vertx.core.buffer.Buffer;

/**
 * @author Robin Duda
 */
public interface ClientRequest extends Request {

    void file(Buffer buffer);

    String PATCH = "patch";
    String GAMEINFO = "gameinfo";
    String NEWS = "news";
    String FILE = "download";
}
