package Meta.Controller;

import Meta.Model.PatchFile;
import Protocols.Request;

/**
 * @author Robin Duda
 */
public interface ClientRequest extends Request {
    void file(PatchFile file);

    String file();

    String version();

    String PATCH = "patch";
    String GAMEINFO = "gameinfo";
    String NEWS = "news";
    String AUTHSERVER = "authserver";
    String FILE = "download";
    String PATCHDATA = "patchdata";
}
