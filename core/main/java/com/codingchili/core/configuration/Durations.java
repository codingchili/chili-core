package com.codingchili.core.configuration;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Durations in the configuration files, written short: {@code 7d}, {@code 10m}, {@code 1h30m}, {@code 250ms}. The units
 * are ms, s, m, h and d, combinable; a plain number is milliseconds (what the configs held before; a field that held
 * seconds or days reads its plain numbers in that unit with {@link ReadSeconds} or {@link ReadDays}). Nothing else is
 * accepted. A config field of type {@link Duration} reads and writes this form with
 * <pre>
 *     &#64;JsonDeserialize(using = Durations.Read.class)
 *     &#64;JsonSerialize(using = Durations.Write.class)
 *     private Duration expiry = Duration.ofDays(7);
 * </pre>
 * The annotations on the field apply to its getter and setter as well.
 */
public final class Durations {
    private static final Pattern NUMBER = Pattern.compile("\\d+");
    private static final Pattern PART = Pattern.compile("(\\d+)(ms|s|m|h|d)");

    private Durations() {
    }

    /**
     * @param text a duration such as 7d, 1h30m or 500ms, or a plain number of milliseconds.
     * @return the duration.
     * @throws IllegalArgumentException naming the value when it is not a duration.
     */
    public static Duration parse(String text) {
        if (text == null || text.isBlank()) {
            throw invalid(text);
        }
        var value = text.strip().toLowerCase(Locale.ROOT);
        try {
            if (NUMBER.matcher(value).matches()) {
                return Duration.ofMillis(Long.parseLong(value));
            }
            var matcher = PART.matcher(value);
            var total = Duration.ZERO;
            var at = 0;
            while (matcher.find()) {
                if (matcher.start() != at) {
                    throw invalid(text);
                }
                var amount = Long.parseLong(matcher.group(1));
                total = total.plus(switch (matcher.group(2)) {
                    case "ms" -> Duration.ofMillis(amount);
                    case "s" -> Duration.ofSeconds(amount);
                    case "m" -> Duration.ofMinutes(amount);
                    case "h" -> Duration.ofHours(amount);
                    default -> Duration.ofDays(amount);
                });
                at = matcher.end();
            }
            if (at == 0 || at != value.length()) {
                throw invalid(text);
            }
            return total;
        } catch (NumberFormatException | ArithmeticException e) {
            throw invalid(text);
        }
    }

    /**
     * @param duration a duration, not negative.
     * @return the short form, the largest units first: 7d, 1h30m, 1s500ms, 0s.
     */
    public static String format(Duration duration) {
        if (duration.isZero()) {
            return "0s";
        }
        var text = new StringBuilder();
        var rest = duration.toMillis();
        long[] sizes = {86_400_000L, 3_600_000L, 60_000L, 1_000L, 1L};
        String[] units = {"d", "h", "m", "s", "ms"};
        for (var i = 0; i < sizes.length; i++) {
            if (rest >= sizes[i]) {
                text.append(rest / sizes[i]).append(units[i]);
                rest %= sizes[i];
            }
        }
        return text.toString();
    }

    /**
     * For the APIs that take an int of milliseconds, such as {@link com.codingchili.core.context.TimerSource} and
     * {@link com.codingchili.core.listener.Request#timeout()}.
     *
     * @param duration a duration.
     * @return the duration in milliseconds, capped at {@link Integer#MAX_VALUE} (24 days).
     */
    public static int millis(Duration duration) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, duration.toMillis()));
    }

    private static IllegalArgumentException invalid(String text) {
        return new IllegalArgumentException(CoreStrings.getInvalidDuration(text));
    }

    /**
     * Reads a config duration: a string in the short form, or a number of milliseconds.
     */
    public static class Read extends ValueDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonParser parser, DeserializationContext context) {
            if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                return Duration.ofMillis(parser.getLongValue());
            }
            return parse(parser.getString());
        }
    }

    /**
     * Reads a config duration of a field that held another unit than milliseconds before: a plain number (also
     * quoted) is that unit, so an old config keeps its meaning; the short form reads as with {@link Read}.
     */
    public abstract static class ReadIn extends ValueDeserializer<Duration> {
        private final ChronoUnit unit;

        /**
         * @param unit the unit of plain numbers.
         */
        protected ReadIn(ChronoUnit unit) {
            this.unit = unit;
        }

        @Override
        public Duration deserialize(JsonParser parser, DeserializationContext context) {
            if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                return Duration.of(parser.getLongValue(), unit);
            }
            var text = parser.getString();
            if (text != null && NUMBER.matcher(text.strip()).matches()) {
                try {
                    return Duration.of(Long.parseLong(text.strip()), unit);
                } catch (NumberFormatException | ArithmeticException e) {
                    throw invalid(text);
                }
            }
            return parse(text);
        }
    }

    /**
     * Reads a config duration whose plain numbers are seconds (30 is 30s).
     */
    public static class ReadSeconds extends ReadIn {
        public ReadSeconds() {
            super(ChronoUnit.SECONDS);
        }
    }

    /**
     * Reads a config duration whose plain numbers are days (14 is 14d).
     */
    public static class ReadDays extends ReadIn {
        public ReadDays() {
            super(ChronoUnit.DAYS);
        }
    }

    /**
     * Writes a config duration in the short form.
     */
    public static class Write extends ValueSerializer<Duration> {
        @Override
        public void serialize(Duration value, JsonGenerator generator, SerializationContext context) {
            generator.writeString(format(value));
        }
    }
}
