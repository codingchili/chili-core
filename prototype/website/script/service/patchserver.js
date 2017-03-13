var patchserver = {
  network: new Network('patching.node'),
    
  patch: function (callback) {
      this.network.send(callback, 'patch');
  }  
};