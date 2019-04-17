package com.codingchili.core.context;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Provides a method to get a timer interval that may change
 * after retrieving it.
 */
public class TimerSource {
    private AtomicBoolean paused = new AtomicBoolean(false);
    private AtomicBoolean terminated = new AtomicBoolean(false);
    private String name = toString();
    private Supplier<Integer> provider;

    private TimerSource() {
    }

    public static TimerSource of(Supplier<Integer> ms) {
        return new TimerSource()
                .setProvider(ms);
    }

    /**
     * @param ms in milliseconds of the timer period.
     * @return a new timer source with the given period
     */
    public static TimerSource of(int ms) {
        return new TimerSource()
                .setPeriod(ms, TimeUnit.MILLISECONDS);
    }

    /**
     * @param value an value of the given unit.
     * @param unit  the unit which the value represents.
     * @return a new timer source with the given period
     */
    public static TimerSource of(int value, TimeUnit unit) {
        return new TimerSource()
                .setPeriod(value, unit);
    }

    /**
     * @param name the name of the timersource.
     * @return fluent
     */
    public TimerSource setName(String name) {
        assertNotTerminated();
        this.name = name;
        return this;
    }

    /**
     * @param value an value of the given unit.
     * @param unit  the unit which the value represents.
     * @return fluent
     */
    public TimerSource setPeriod(int value, TimeUnit unit) {
        assertNotTerminated();
        this.provider = () -> (int) unit.toMillis(value);
        return this;
    }

    /**
     * @param ms the time period in milliseconds to set.
     * @return fluent
     */
    public TimerSource setMS(int ms) {
        assertNotTerminated();
        this.provider = () -> ms;
        return this;
    }

    private void assertNotTerminated() {
        if (terminated.get()) {
            throw new TimerSourceException("Cannot modify a terminated TimerSource instance.");
        }
    }

    /**
     * @return Returns the interval length in MS.
     */
    public int getMS() {
        return provider.get();
    }

    /**
     * @param provider another dynamic source of timing, configuration etc.
     * @return fluent
     */
    public TimerSource setProvider(Supplier<Integer> provider) {
        this.provider = provider;
        return this;
    }

    /**
     * @return the name of the timer if set otherwise the instance address.
     */
    public String getName() {
        return name;
    }

    /**
     * the timer will continue to tick but the scheduled operation will not be executed.
     * note: has no effect if already paused.
     */
    public void pause() {
        assertNotTerminated();
        paused.set(true);
    }

    /**
     * un-pauses the timersource, the timer operation will now be invoked at each interval again.
     * note: has no effect if already unpaused.
     */
    public void unpause() {
        assertNotTerminated();
        paused.set(false);
    }

    /**
     * @return true if the timer source is paused.
     */
    public boolean isPaused() {
        return paused.get();
    }

    /**
     * Terminates the timer source instance, this timer can not be started again.
     */
    public void terminate() {
        assertNotTerminated();
        terminated.set(true);
    }

    /**
     * @return true if the timer source is terminated and the timer should be de-scheduled.
     */
    public boolean isTerminated() {
        return terminated.get();
    }
}
