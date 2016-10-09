package com.codingchili.core.Logging.Controller;

import com.codingchili.core.Logging.Configuration.LogProvider;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Logging.Model.ConsoleLogger;
import com.codingchili.core.Logging.Model.ElasticLogger;
import com.codingchili.core.Protocols.AbstractHandler;
import com.codingchili.core.Protocols.Access;
import com.codingchili.core.Protocols.Exception.AuthorizationRequiredException;
import com.codingchili.core.Protocols.Exception.HandlerMissingException;
import com.codingchili.core.Protocols.Exception.ProtocolException;
import com.codingchili.core.Protocols.Request;
import com.codingchili.core.Protocols.RequestHandler;
import com.codingchili.core.Protocols.Util.Protocol;
import com.codingchili.core.Protocols.Util.TokenFactory;
import io.vertx.core.json.JsonObject;

import static com.codingchili.core.Configuration.Strings.*;
import static com.codingchili.core.Protocols.Access.AUTHORIZED;
import static com.codingchili.core.Protocols.Access.PUBLIC;


/**
 * @author Robin Duda
 */
public class LogHandler extends AbstractHandler {
    private Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private TokenFactory tokenFactory;
    private ConsoleLogger console;
    private ElasticLogger elastic;

    public LogHandler(LogProvider provider) {
        super(NODE_LOGGING);

        LogServerSettings settings = provider.getSettings();
        this.tokenFactory = new TokenFactory(settings.getSecret());
        this.console = new ConsoleLogger(settings.getConsole());
        this.elastic = new ElasticLogger(settings.getElastic(), provider.getVertx());

        protocol.use(PROTOCOL_LOGGING, this::log);
    }

    private void log(Request request) {
        JsonObject logdata = request.data();

        logdata.remove(ID_TOKEN);
        logdata.remove(PROTOCOL_ACTION);

        elastic.log(logdata);
        console.log(logdata);
    }

    @Override
    public void handle(Request request) throws ProtocolException {
        protocol.get(authenticator(request), request.action()).handle(request);
    }

    private Access authenticator(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            return AUTHORIZED;
        } else {
            return PUBLIC;
        }
    }
}
