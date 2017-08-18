function Network(service) {
    var self = this;
    this.remote = '/';
    this.port = 80;
    this.service = service;

    this.send = function (callback, route, json) {
        if (!json) {
            json = {};
        }
        json.target = service;
        json.route = route;

        // todo provide multiple transport implementations here.
        $.ajax({
            type: "POST",
            url: this.remote,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(json),
            success: function (data) {
                if (callback.any) {
                    callback.any(data);
                } else {
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
            },
            error: function (code) {
                var error = {'message': 'Network error (' + JSON.stringify(code) + ')', 'status': ResponseStatus.ERROR}
                if (callback.failed) {
                    callback.failed(error);
                } else {
                    console.log('Unhandled network error: ' + JSON.stringify(error));
                }
            }
        });
    };

    this.ping = function (callback) {
        self.send(callback, 'ping', {});
    };

    return this;
}

var ResponseStatus = {
    ACCEPTED: 'ACCEPTED',
    BAD: 'BAD',
    MISSING: 'MISSING',
    CONFLICT: 'CONFLICT',
    UNAUTHORIZED: 'UNAUTHORIZED',
    ERROR: 'ERROR',
};