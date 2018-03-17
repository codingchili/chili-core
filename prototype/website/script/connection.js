/**
 * handles network communication for websockets.
 */
class Connection {

    constructor(host, port) {
        this.port = port;
        this.host = host;
        this.handlers = {};

        this.ws = new WebSocket("wss://" + this.host + ":" + this.port + "/");
        this.ws.onmessage = (msg) => {
            this.onmessage(msg);
        };
        this.ws.onopen = () => {
            this.open = true;
            if (this.connected != null) {
                this.connected();
            }
        }
        this.ws.onerror = (evt) => {
            this.onerror(evt);
        };
    }

    onConnected(connected) {
        this.connected = connected;
    }

    send(callback, route, data) {
        setTimeout(() => {
            this.handlers[route] = callback;
            data.route = route;
            this.ws.send(JSON.stringify(data));
        }, (this.connected) ? 1 : 200);
    }

    addHandler(route, handler) {
        console.log('Connection::addHandler()_noop');
    }

    onmessage(event) {
       let data = JSON.parse(event.data);

       if (this.handlers[data.route]) {
         if (data.status === ResponseStatus.ACCEPTED) {
            this.handlers[data.route].accepted(data);
         } else {
            this.handlers[data.route].error(data);
         }
       } else {
         console.log('no handler for message: ' + event.data);
       }
    }

    onerror(event) {
        application.error('Server error: connection closed.');
    }

    onclose(event) {
        if (!event.wasClean)
            application.error('The connection to the ' + this.realm.name + ' server was lost, please retry.');
    }
}