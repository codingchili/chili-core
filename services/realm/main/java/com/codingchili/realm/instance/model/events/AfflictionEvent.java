package com.codingchili.realm.instance.model.events;

import com.codingchili.realm.instance.model.afflictions.ActiveAffliction;

/**
 * @author Robin Duda
 */
public class AfflictionEvent implements Event {
    private ActiveAffliction active;

    public AfflictionEvent(ActiveAffliction affliction) {
        this.active = affliction;
    }

    public String getSourceId() {
        return active.getSource().getId();
    }

    public String getTargetId() {
        return active.getTarget().getId();
    }

    public String getName() {
        return active.getAffliction().getName();
    }

    public String getDescription() {
        return active.getAffliction().getDescription();
    }

    public Float getDuration() {
        return active.getAffliction().getDuration();
    }

    @Override
    public EventType getType() {
        return EventType.AFFLICTION;
    }
}
