package Game.Model;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-25.
 */
public class Spell {
    private String name;
    private String description;
    private Target target;
    private Double cooldown;
    private Double casttime;
    private Cost cost;
    private ArrayList<Modifier> modifier;
    private Boolean passive = false;

    public Cost getCost() {
        return cost;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
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

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Double getCooldown() {
        return cooldown;
    }

    public void setCooldown(Double cooldown) {
        this.cooldown = cooldown;
    }

    public Double getCasttime() {
        return casttime;
    }

    public void setCasttime(Double casttime) {
        this.casttime = casttime;
    }

    public ArrayList<Modifier> getModifier() {
        return modifier;
    }

    public void setModifier(ArrayList<Modifier> modifier) {
        this.modifier = modifier;
    }

    public Boolean isPassive() {
        return passive;
    }

    public void setPassive(Boolean passive) {
        this.passive = passive;
    }
}
