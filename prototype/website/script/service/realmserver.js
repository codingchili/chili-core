/**
 * Realmserver api.
 */
class RealmServer {

    constructor(realm) {
        this.realm = realm;
        this.network = new Network(realm.remote);
        this.ping = this.network.ping;
    }

    characterlist(callback) {
        this.network.send(callback, 'character.list', {token: this.realm.token});
    }

    create(callback, className, characterName) {
        this.network.send(callback, 'character.create', {
            token: this.realm.token,
            className: className,
            character: characterName
        });
    }

    remove(callback, characterName) {
        this.network.send(callback, 'character.remove', {
            token: this.realm.token,
            realm: this.realm.name,
            character: characterName
        });
    }

    static ping(callback, realm) {
        new Network(realm.remote).ping(callback);
    }
}