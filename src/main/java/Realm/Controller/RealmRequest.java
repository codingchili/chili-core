package Realm.Controller;

import Protocols.Realm.CharacterRequest;
import Protocols.Request;

/**
 * @author Robin Duda
 */
interface RealmRequest extends Request {
    CharacterRequest characterRequest();
}
