package Patching.Controller;

import Patching.Model.PatchFile;
import Protocols.Request;

/**
 * @author Robin Duda
 */
public interface ClientRequest extends Request {
    void file(PatchFile file);

    String file();

    String version();
}
