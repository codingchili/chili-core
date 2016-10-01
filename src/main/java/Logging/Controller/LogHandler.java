package Logging.Controller;

import Logging.Configuration.LogProvider;
import Logging.Configuration.LogServerSettings;
import Logging.Model.ConsoleLogger;
import Logging.Model.ElasticLogger;
import Protocols.*;
import Protocols.Util.Protocol;
import Protocols.Util.TokenFactory;
import Protocols.Exception.AuthorizationRequiredException;
import Protocols.Exception.HandlerMissingException;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.*;
import static Protocols.Access.AUTHORIZED;
import static Protocols.Access.PUBLIC;


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

    private Access authenticator(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            return AUTHORIZED;
        } else {
            return PUBLIC;
        }
    }

    @Override
    public void handle(Request request) {
        try {
            protocol.get(authenticator(request), request.action()).handle(request);
        } catch (AuthorizationRequiredException e) {
            request.unauthorized();
        } catch (HandlerMissingException e) {
            request.error();
        }
    }

    private void log(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            JsonObject logdata = request.data();

            logdata.remove(ID_TOKEN);
            logdata.remove(PROTOCOL_ACTION);

            elastic.log(logdata);
            console.log(logdata);
        }
    }
}
