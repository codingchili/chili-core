package Game.Model;

/**
 * Created by Robin on 2016-04-24.
 */
public class Character {
    private String name;
    private Equipment equipment;            // equipped items
    private Attributes attributes;          // base stats
    private Regeneration regeneration;
    private double health;                  // calculated stats from attributes + equipment (+afflictions?)
    private double power;
    private double resistance;
    private double defence;
    private double attack;
    private double spell;
    private double speed;
    private double haste;

    public Character(String name) {
        this.name = name;
    }
}
