var realmregistry = {
    network : new Network('client.realmregistry.node'),
    
    list: function(callback) {
        this.network.send(callback, 'realmlist');
    },

    realmtoken: function(callback, realmName) {
        this.network.send(callback, 'realmtoken',
        {
            'realm' : realmName,
            'token': application.authentication.token
        });
    }
};