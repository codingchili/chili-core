/**
 * @author Robin Duda
 *
 * Used to pass application-level events between components.
 */
var configuration = {
    api: function (method) {
        return "http://localhost:5505/api/" + method;
    }
};

var application = {
    authentication: null,
    handlers: {},

    authenticated: function (authentication) {
        console.log(authentication);

        this.authentication = authentication;
        $('#game-login').hide();
        $('#page').hide();
        $('#realm-list').show();

        this.publish('onAuthentication', authentication);
    },

    onAuthentication: function (callback) {
        this.subscribe('onAuthentication', callback);
    },

    logout: function () {
        $('#game-login').show();
        $('#game-view').hide();
        $('#realm-list').hide();

        this.publish('onLogout', {});
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
    $('#game-view').hide();
    $('#game-login').hide();
    $('#realm-list').hide();

    application.subscribe("game-login", function () {
        $('#page').hide();
        $('#game-login').show();
    });


    /* setTimeout(function () {
     $('#game-login').hide();
     $('#page').hide();
     //application.publish("game-login", {});
     application.authenticated({account: {username: "spinx"}});
     }, 500);
     */
});