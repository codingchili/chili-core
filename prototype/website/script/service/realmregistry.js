var realmregistry = {
    network : new Network('client.realm.node'),
    
    list: function(callback) {
        this.network.send(callback, 'list');
    }
};