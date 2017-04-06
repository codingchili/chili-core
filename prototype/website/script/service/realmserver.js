var realmserver = function (realm){
    this.network = new Network(realm.remote);
    this.ping = this.network.ping;
    return this;
};