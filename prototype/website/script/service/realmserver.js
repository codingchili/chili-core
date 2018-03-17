/**
 * Realmserver api.
 */
class RealmServer {

    constructor(realm) {
        this.realm = realm;
        this.connection = new Connection(realm.host, realm.port);
    }

    characterlist(callback) {
        this.connection.send(callback, 'character.list', {token: this.realm.token});
    }

    create(callback, className, characterName) {
        this.connection.send(callback, 'character.create', {
            token: this.realm.token,
            className: className,
            character: characterName
        });
    }

    remove(callback, characterName) {
        this.connection.send(callback, 'character.remove', {
            token: this.realm.token,
            realm: this.realm.name,
            character: characterName
        });
    }

    static ping(callback, realm) {
        new Network()
            .setPort(realm.port)
            .setHost(realm.host)
            .ping(callback);
    }
}