package Game.Model;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-24.
 */
public class Modifier {
    private ArrayList<Affliction> affliction;
    private Target modifies = Target.caster;
    private Attribute attribute;
    private Using using;

    public ArrayList<Affliction> getAffliction() {
        return affliction;
    }

    public void setAffliction(ArrayList<Affliction> affliction) {
        this.affliction = affliction;
    }

    public Target getModifies() {
        return modifies;
    }

    public void setModifies(Target modifies) {
        this.modifies = modifies;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Using getUsing() {
        return using;
    }

    public void setUsing(Using using) {
        this.using = using;
    }
}
