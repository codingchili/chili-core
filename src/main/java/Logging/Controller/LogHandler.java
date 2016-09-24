package Logging.Controller;

import Logging.Configuration.LogProvider;
import Logging.Configuration.LogServerSettings;
import Logging.Model.ConsoleLogger;
import Logging.Model.ElasticLogger;
import Protocols.*;
import Protocols.Authorization.TokenFactory;
import io.vertx.core.json.JsonObject;

import static Configuration.Strings.*;
import static Protocols.Access.AUTHORIZE;
import static Protocols.Access.PUBLIC;


/**
 * @author Robin Duda
 */
public class LogHandler extends HandlerProvider {
    private TokenFactory tokenFactory;
    private ConsoleLogger console;
    private ElasticLogger elastic;

    public LogHandler(LogProvider provider) {
        super(LogHandler.class, provider.getLogger(), ADDRESS_LOGGING);

        LogServerSettings settings = provider.getSettings();
        this.tokenFactory = new TokenFactory(settings.getSecret());
        this.console = new ConsoleLogger(settings.getConsole());
        this.elastic = new ElasticLogger(settings.getElastic(), provider.getVertx());
    }

    @Authenticator
    public Access authenticator(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            return AUTHORIZE;
        } else {
            return PUBLIC;
        }
    }

    @Handles(PROTOCOL_LOGGING)
    public void log(Request request) {
        if (tokenFactory.verifyToken(request.token())) {
            JsonObject logdata = request.data();

            logdata.remove(ID_TOKEN);
            logdata.remove(PROTOCOL_ACTION);

            elastic.log(logdata);
            console.log(logdata);
        }
    }
}
