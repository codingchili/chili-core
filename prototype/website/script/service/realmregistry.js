/**
 * Handles the connection to the realm registry.
 */
class RealmRegistry {

    constructor() {
        this.network = new Network('client.realmregistry.node');
    }

    list(callback) {
        this.network.send(callback, 'realmlist');
    }

    realmtoken(callback, realmName) {
        this.network.send(callback, 'realmtoken', {
           'realm': realmName,
            'token': application.authentication.token
        });
    }
}