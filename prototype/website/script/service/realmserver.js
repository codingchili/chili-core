var realmserver = function (realm){
    this.network = new Network(realm);
    this.ping = this.network.ping;
    return this;
};