package Game.Model;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-24.
 */
public class Affliction {
    private String name;
    private String description;
    private Target target;
    private Double rate;
    private Double chance;
    private Double duration;
    private Boolean everlasting;
    private ArrayList<Modifier> modifier;
    private ArrayList<Trigger> trigger;
    private ArrayList<Affliction> affliction;

    public Boolean getEverlasting() {
        return everlasting;
    }

    public ArrayList<Affliction> getAffliction() {
        return affliction;
    }

    public void setAffliction(ArrayList<Affliction> affliction) {
        this.affliction = affliction;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Boolean isEverlasting() {
        return everlasting;
    }

    public void setEverlasting(Boolean everlasting) {
        this.everlasting = everlasting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Modifier> getModifier() {
        return modifier;
    }

    public void setModifier(ArrayList<Modifier> modifier) {
        this.modifier = modifier;
    }

    public ArrayList<Trigger> getTrigger() {
        return trigger;
    }

    public void setTrigger(ArrayList<Trigger> trigger) {
        this.trigger = trigger;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getChance() {
        return chance;
    }

    public void setChance(Double chance) {
        this.chance = chance;
    }
}
