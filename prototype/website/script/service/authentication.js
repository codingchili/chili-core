var authentication = {
    network: new Network('client.authentication.node'),

    login: function (callback, username, password) {
        this.network.send(callback, 'authenticate', {
            'username': username,
            'password': password
        });
    },

    register: function (callback, account) {

    }

};