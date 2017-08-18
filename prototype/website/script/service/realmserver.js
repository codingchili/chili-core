class RealmServer {

    constructor (realm) {
        this.realm = realm;
        this.network = new Network(realm.remote);
        this.ping = this.network.ping;
    }

    characterlist (callback) {
        this.network.send(callback, "characterlist", {token : this.realm.token});
    }

    static ping (callback, realm) {
        new Network(realm.remote).ping(callback);
    }
}