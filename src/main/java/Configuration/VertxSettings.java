package Configuration;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.metrics.MetricsOptions;

/**
 * @author Robin Duda
 */
public class VertxSettings {
    public static int METRIC_RATE = 0;
    public static boolean METRICS_ENABLED;
    private JsonArray help;

    public static VertxOptions Configuration() {
        return new VertxOptions().setMetricsOptions(new MetricsOptions().setEnabled(METRICS_ENABLED));
    }

    public JsonArray getHelp() {
        return help;
    }

    public void setHelp(JsonArray help) {
        this.help = help;
    }

    public boolean isMetrics() {
        return METRICS_ENABLED;
    }

    public void setMetrics(boolean metrics) {
        VertxSettings.METRICS_ENABLED = metrics;
    }

    public int getRate() {
        return VertxSettings.METRIC_RATE;
    }

    public void setRate(int rate) {
        VertxSettings.METRIC_RATE = rate;
    }
}
