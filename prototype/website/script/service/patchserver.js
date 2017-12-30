/**
 * Handles the patching api.
 */
class PatchServer {

    constructor() {
        this.network = new Network('patching.node');
    }

    patch(callback) {
        this.network.send(callback, 'patch');
    }

    gameinfo(callback) {
        this.network.send(callback, 'gameinfo');
    }

    news(callback) {
        this.network.send(callback, 'news');
    }
}