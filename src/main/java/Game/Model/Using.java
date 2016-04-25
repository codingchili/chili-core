package Game.Model;

/**
 * Created by Robin on 2016-04-25.
 */
public class Using {
    private Target source = Target.caster;
    private Attribute attribute;
    private Double value;
    private Boolean cancel;

    public Boolean isCancel() {
        return cancel;
    }

    public void setCancel(Boolean cancel) {
        this.cancel = cancel;
    }

    public Target getSource() {
        return source;
    }

    public void setSource(Target source) {
        this.source = source;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
