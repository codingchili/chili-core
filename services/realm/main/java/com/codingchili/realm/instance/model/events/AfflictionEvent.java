package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.afflictions.ActiveAffliction;

/**
 * @author Robin Duda
 */
public class AfflictionEvent implements Event {
    private ActiveAffliction affliction;

    public AfflictionEvent(ActiveAffliction affliction) {
        this.affliction = affliction;
    }

    public ActiveAffliction getAffliction() {
        return affliction;
    }

    public void setAffliction(ActiveAffliction affliction) {
        this.affliction = affliction;
    }

    @Override
    public EventType getType() {
        return EventType.AFFLICTION;
    }
}
