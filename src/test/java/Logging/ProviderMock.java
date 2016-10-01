package Logging;

import Configuration.ConfigMock;
import Logging.Configuration.LogProvider;
import Logging.Configuration.LogServerSettings;
import Logging.Model.Logger;

/**
 * @author Robin Duda
 */
class ProviderMock extends LogProvider {

    ProviderMock() {
        super();
    }

    @Override
    public LogServerSettings getSettings() {
        return new ConfigMock.LogServerSettingsMock();
    }

    @Override
    public Logger getLogger() {
        return null;
    }
}
