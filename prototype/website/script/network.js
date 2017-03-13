function Network(service) {
    this.remote = '/';
    this.port = 80;
    this.service = service;

    this.send = function (callback, route, json) {
        if (!json) {
            json = {};
        }
        json.target = service;
        json.route = route;
        $.ajax({
            type: "POST",
            url: this.remote,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(json),
            success: function (data) {
                if (data.status == ResponseStatus.ACCEPTED) {
                    callback.success(data);
                } else {
                    callback.failed(data);
                }
            },
            error: function (code) {
                callback.failed({
                        'message': 'Network error (' + code + ')',
                        'status': ResponseStatus.ERROR
                    }
                );
            }
        });
    };

    this.ping = function (callback) {
        this.send(callback, 'ping', {});
    };

    return this;
}

var ResponseStatus = {
    ERROR: 'ERROR',
    BAD: 'BAD',
    ACCEPTED: 'ACCEPTED'
};