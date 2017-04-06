var realmregistry = {
    network : new Network('client.realmregistry.node'),
    
    list: function(callback) {
        this.network.send(callback, 'realmlist');
    }
};