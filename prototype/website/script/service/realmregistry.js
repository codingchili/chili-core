/**
 * Handles the connection to the realm registry.
 */
class RealmRegistry {

    constructor() {
        this.network = new Network('client.realmregistry.node');
    }

    list(callback) {
        this.network.rest(callback, 'realmlist');
    }

    realmtoken(callback, realmName) {
        this.network.rest(callback, 'realmtoken', {
           'realm': realmName,
            'token': application.authentication.token
        });
    }
}