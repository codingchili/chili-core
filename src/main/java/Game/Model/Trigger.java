package Game.Model;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-25.
 */
public class Trigger {
    private Event on;
    private String spell;
    private Target target;
    private ArrayList<Modifier> modifier;
    private ArrayList<Affliction> affliction;

    public Event getOn() {
        return on;
    }

    public void setOn(Event on) {
        this.on = on;
    }

    public String getSpell() {
        return spell;
    }

    public void setSpell(String spell) {
        this.spell = spell;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public ArrayList<Modifier> getModifier() {
        return modifier;
    }

    public void setModifier(ArrayList<Modifier> modifier) {
        this.modifier = modifier;
    }

    public ArrayList<Affliction> getAffliction() {
        return affliction;
    }

    public void setAffliction(ArrayList<Affliction> affliction) {
        this.affliction = affliction;
    }
}



