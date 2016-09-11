package Routing.Model;

import Configuration.ConfigurationLoader;
import Configuration.FileConfiguration;
import Configuration.Strings;
import Protocols.Protocol;

import java.util.HashMap;

/**
 * @author Robin Duda
 *         <p>
 *         Maps handlers that are registered on protocols from the nodes and
 *         adds addressing to that node using a map.
 */
public abstract class RequestMapper {
    private static HashMap<String, String> mappings = generateMappings();
    private static ConfigurationLoader configuration = FileConfiguration.instance();

    public static String getAddress(String action) throws NoSuchMappingException {
        if (mappings.containsKey(action)) {
            return mappings.get(action);
        } else {
            throw new NoSuchMappingException();
        }
    }

    private static HashMap<String, String> generateMappings() {
        HashMap<String, String> map = new HashMap<>();

        map.putAll(authentication());
        map.putAll(patching());
        map.putAll(logging());
        map.putAll(webserver());
        map.putAll(realm());

        return map;
    }

    private static HashMap<String, String> authentication() {
        HashMap<String, String> list = new HashMap<>();

        list.putAll(listHandlers(
                new Authentication.Controller.ClientHandler(null)
                        .apply(new Protocol<>()),
                Strings.ADDRESS_AUTHENTICATION));

        list.putAll(listHandlers(
                new Authentication.Controller.RealmHandler(null)
                        .apply(new Protocol<>()),
                Strings.ADDRESS_AUTHENTICATION));

        return list;
    }

    private static HashMap<String, String> patching() {
        return listHandlers(
                new Patching.Controller.ClientHandler(null)
                        .apply(new Protocol<>()),
                Strings.ADDRESS_PATCHING);
    }

    private static HashMap<String, String> logging() {
        //return listHandlers(new Logging.Controller.LogHandler())
        // todo the logger must implement packet handlers.
        return new HashMap<>();
    }

    private static HashMap<String, String> webserver() {
        HashMap<String, String> map = new HashMap<>();

        // todo the webserver must implement packet handlers.

        return map;
    }

    private static HashMap<String, String> realm() {
        HashMap<String, String> map = new HashMap<>();

        // todo the realmserver must implement packet handlers.

        return map;
    }

    private static HashMap<String, String> listHandlers(Protocol protocol, String address) {
        HashMap<String, String> handlers = new HashMap<>();

        for (Object key : protocol.list().keySet()) {
            handlers.put(key.toString(), address);
        }

        return handlers;
    }
}
