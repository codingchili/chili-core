package Realm.Model;

/**
 * @author Robin Duda
 *         Holds the cost of a spell.
 */
class Cost {
    private Attribute requires;
    private Integer value;

    public Attribute getRequires() {
        return requires;
    }

    public void setRequires(Attribute requires) {
        this.requires = requires;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
