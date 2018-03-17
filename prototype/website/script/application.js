/**
 * @author Robin Duda
 *
 * Used to pass application-level events between components.
 */
class Application {
    
    constructor() {
        this.views = ['realm-list', 'page', 'game-view', 'game-login', 'character-list', 'patch-download', 'error-dialog'];
        this.authentication = null
        this.handlers = {};
    }

    authenticated(authentication) {
        application.authentication = authentication;
        application.view('realm-list');
        application.publish('onAuthentication', authentication);
    }

    error(error) {
        application.publish('onLogout', {});
        application.view('error-dialog');
        application.publish('onError', {text: error, callback: application.showLogin});
    }

    selectRealm(realm) {
        application.realm = realm;
        application.showCharacters();
    }

    logout() {
        application.publish('onLogout', {});
        application.showLogin();
    }

    realmLoaded(event) {
        application.publish('onRealmLoaded', event);
    }

    update(event) {
        application.publish('onBeginUpdate', event);
        application.showPatcher();
    }

    updateComplete(event) {
        application.publish('onCompleteUpdate', event);
        application.showGame();
    }

    loadedVersion(event) {
        application.publish('onVersion', event);
        this.version = event;
    }

    onAuthentication(callback) {
        application.subscribe('onAuthentication', callback);
    }

    onRealmSelect(callback) {
        application.subscribe('onRealmSelect', callback);
    }

    onError(callback) {
        application.subscribe('onError', callback);
    }

    onLogout(callback) {
        application.subscribe('onLogout', callback);
    }

    onRealmLoaded(callback) {
        application.subscribe('onRealmLoaded', callback);
    }

    onUpdate(callback) {
        application.subscribe('onBeginUpdate', callback);
    }

    onGameStart(callback) {
        application.subscribe('onGameStart', callback);
    }

    onVersion(callback) {
        application.subscribe('onVersion', callback);

        if (this.version) {
            callback(this.version)
        }
    }

    showLogin() {
        application.view('game-login');
    }

    showRealms() {
        application.view('realm-list');
        application.authenticated(application.authentication);
    }

    showCharacters() {
        application.publish('onRealmSelect', application.realm);
        application.view('character-list');
    }

    showPatcher() {
        application.view('patch-download');
    }

    showGame() {
        application.view('game-view');
        application.publish('onGameStart', {});
    }

    showStart() {
        application.view('page');
        application.publish('onViewStart', {});
    }

    view(view) {
        for (var i = 0; i < this.views.length; i++) {
            if (this.views[i] === view)
                document.getElementById(this.views[i]).style.display = 'block';
            else
                document.getElementById(this.views[i]).style.display= 'none';
        }
    }

    subscribe(event, callback) {
        if (this.handlers[event] == null)
            this.handlers[event] = [];

        this.handlers[event].push(callback);
    }

    publish(event, data) {
        if (this.handlers[event])
            for (let subscriber = 0; subscriber < this.handlers[event].length; subscriber++)
                this.handlers[event][subscriber](data);
    }
}

var application = new Application();

document.addEventListener('DOMContentLoaded', function() {
    application.view('game-login');
});