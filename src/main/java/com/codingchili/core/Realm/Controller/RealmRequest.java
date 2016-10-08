package com.codingchili.core.Realm.Controller;

import com.codingchili.core.Protocols.Realm.CharacterRequest;
import com.codingchili.core.Protocols.Request;

/**
 * @author Robin Duda
 */
interface RealmRequest extends Request {
    CharacterRequest characterRequest();
}
