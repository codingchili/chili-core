package Game.Model;

/**
 * Created by Robin on 2016-04-24.
 */
public class Character {
    private String name;
    private String realm;
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

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Regeneration getRegeneration() {
        return regeneration;
    }

    public void setRegeneration(Regeneration regeneration) {
        this.regeneration = regeneration;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public double getDefence() {
        return defence;
    }

    public void setDefence(double defence) {
        this.defence = defence;
    }

    public double getAttack() {
        return attack;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public double getSpell() {
        return spell;
    }

    public void setSpell(double spell) {
        this.spell = spell;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getHaste() {
        return haste;
    }

    public void setHaste(double haste) {
        this.haste = haste;
    }
}
