/**
 * @author Robin Duda
 *
 * Used to pass application-level events between components.
 */
var application = {
    views: ['realm-list', 'page', 'game-view', 'game-login', 'character-list', 'patch-download', 'error-dialog'],
    authentication: null,
    handlers: {},

    authenticated: function (authentication) {
        application.authentication = authentication;
        application.view('realm-list');
        application.publish('onAuthentication', authentication);
    },

    error: function (error) {
        application.publish('onLogout', {});
        application.view('error-dialog');
        application.publish('onError', {text: error, callback: application.showLogin});
    },

    selectRealm: function (realm) {
        application.realm = realm;
        application.showCharacters();
    },

    logout: function () {
        application.publish('onLogout', {});
        application.showLogin();
    },

    realmLoaded: function (event) {
        application.publish('onRealmLoaded', event);
    },

    update: function (event) {
        application.publish('onBeginUpdate', event);
        application.showPatcher();
    },

    updateComplete: function (event) {
        application.publish('onCompleteUpdate', event);
        application.showGame();
    },

    loadedVersion: function (event) {
        application.publish('onVersion', event);
    },

    onAuthentication: function (callback) {
        application.subscribe('onAuthentication', callback);
    },

    onRealmSelect: function (callback) {
        application.subscribe('onRealmSelect', callback);
    },

    onError: function (callback) {
        application.subscribe('onError', callback);
    },

    onLogout: function (callback) {
        application.subscribe('onLogout', callback);
    },

    onRealmLoaded: function (callback) {
        application.subscribe('onRealmLoaded', callback);
    },

    onUpdate: function (callback) {
        application.subscribe('onBeginUpdate', callback);
    },

    onGameStart: function (callback) {
        application.subscribe('onGameStart', callback);
    },

    onVersion: function (callback) {
        application.subscribe('onVersion', callback);
    },

    showLogin: function () {
        application.view('game-login');
    },

    showRealms: function () {
        application.view('realm-list');
        application.authenticated(application.authentication);
    },

    showCharacters: function () {
        application.publish('onRealmSelect', application.realm);
        application.view('character-list');
    },

    showPatcher: function () {
        application.view('patch-download');
    },

    showGame: function () {
        application.view('game-view');
        application.publish('onGameStart', {});
    },

    view: function (view) {
        for (var i = 0; i < this.views.length; i++) {
            if (this.views[i] == view)
                $('#' + this.views[i]).show();
            else
                $('#' + this.views[i]).hide();
        }
    },

    subscribe: function (event, callback) {
        if (this.handlers[event] == null)
            this.handlers[event] = [];

        this.handlers[event].push(callback);
    },

    publish: function (event, data) {
        if (this.handlers[event])
            for (var subscriber = 0; subscriber < this.handlers[event].length; subscriber++)
                this.handlers[event][subscriber](data);
    }
};

$(document).ready(function () {
    application.view('game-login');
});
