/**
 * handles network communication with backend api's using REST.
 */
class Network {

    constructor(service) {
        this.remote = '/';
        this.port = 1443; // default api port.
        this.service = service;
        this.host = window.location.hostname
    }

    setHost(host) {
        this.host = host;
        return this;
    }

    setPort(port) {
        this.port = port;
        return this;
    }

    ping(callback) {
        this.rest(callback, 'ping', {});
    }

    rest(callback, route, json) {
        if (!json) {
            json = {};
        }
        json.target = this.service;
        json.route = route;

        let response = new XMLHttpRequest();
        let url = '//' + this.host + ':' + this.port + this.remote;
        response.open("POST", url);
        response.setRequestHeader("Content-Type", "application/json");
        response.send(JSON.stringify(json));

        response.onreadystatechange = (response) => {
            response = response.currentTarget;

            if (response.readyState == 4) {
                if (response.status >= 200 && response.status <= 300) {
                    var data = JSON.parse(response.responseText);

                    if (callback.any) {
                        callback.any(data);
                    } else {
                        this.handleResponse(data, callback);
                    }
                } else {
                    this.handleError(response, json, callback);
                }
            }
        }
    }

    handleResponse(data, callback) {
        switch (data.status) {
            case ResponseStatus.ACCEPTED:
                if (callback.accepted) {
                    callback.accepted(data);
                }
                break;
            case ResponseStatus.BAD:
                if (callback.bad) {
                    callback.bad(data);
                    break;
                }
            case ResponseStatus.MISSING:
                if (callback.missing) {
                    callback.missing(data);
                    break;
                }
            case ResponseStatus.CONFLICT:
                if (callback.conflict) {
                    callback.conflict(data);
                    break;
                }
            case ResponseStatus.UNAUTHORIZED:
                if (callback.unauthorized) {
                    callback.unauthorized(data);
                    break;
                }
            default:
                if (callback.error) {
                    callback.error(data);
                } else {
                    console.log('Unhandled protocol error: ' + JSON.stringify(data));
                }
        }
    }


    handleError(response, json, callback) {
        const error = {'message': 'Network error ' + JSON.stringify(response.status) + ' for message ' +
            JSON.stringify(json), 'status': ResponseStatus.ERROR};

        if (callback.failed) {
            callback.failed(error);
        } else {
            console.log('Unhandled network error: ' + JSON.stringify(error));
        }
    }
}

const ResponseStatus = {
    ACCEPTED: 'ACCEPTED',
    BAD: 'BAD',
    MISSING: 'MISSING',
    CONFLICT: 'CONFLICT',
    UNAUTHORIZED: 'UNAUTHORIZED',
    ERROR: 'ERROR',
};