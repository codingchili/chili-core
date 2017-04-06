var authentication = {
    network: new Network('client.authentication.node'),

    login: function (callback, username, password) {
        this.network.send(callback, 'authenticate', {account: {
                'username': username,
                'password': password
            }
        });
    },

    register: function (callback, username, password, email) {
        this.network.send(callback, 'register',
        {account: {username:username, password:password, email:email}});
    }
};