/**
 * Handles the patching api.
 */
class PatchServer {

    constructor() {
        this.network = new Network('patching.node');
    }

    patch(callback) {
        this.network.rest(callback, 'patch');
    }

    gameinfo(callback) {
        this.network.rest(callback, 'gameinfo');
    }

    news(callback) {
        this.network.rest(callback, 'news');
    }
}