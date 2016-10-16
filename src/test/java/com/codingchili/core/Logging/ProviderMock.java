package com.codingchili.core.Logging;

import com.codingchili.core.Logging.Configuration.LogProvider;
import com.codingchili.core.Logging.Configuration.LogServerSettings;
import com.codingchili.core.Logging.Model.Logger;

/**
 * @author Robin Duda
 */
class ProviderMock extends LogProvider {
    private LogServerSettings settings;

    ProviderMock(LogServerSettings settings) {
        super();
        settings.getElastic().setEnabled(false);
        this.settings = settings;
    }

    @Override
    public LogServerSettings getSettings() {
        return settings;
    }

    @Override
    public Logger getLogger() {
        return new LoggerMock();
    }
}
