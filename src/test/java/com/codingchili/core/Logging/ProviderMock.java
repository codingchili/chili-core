package com.codingchili.core.Logging;

import com.codingchili.core.Configuration.ConfigMock;
import com.codingchili.core.Logging.Configuration.LogProvider;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Logging.Model.Logger;

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
        return new LoggerMock();
    }
}
