/**
 * Client authentication API.
 */

class Authentication {

    constructor() {
        this.network = new Network('client.authentication.node');
    }

    login(callback, username, password) {
        this.network.send(callback, 'authenticate', {account: {
            'username': username,
            'password': password
        }
        });
    }

    register(callback, username, password, email) {
        this.network.send(callback, 'register',
            {account: {username:username, password:password, email:email}});
    }
}