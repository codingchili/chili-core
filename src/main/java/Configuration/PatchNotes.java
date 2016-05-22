package Configuration;

import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains a list of patch notes, build and name for the website.
 */
public class PatchNotes {
    private String build;
    private String name;
    private String date;
    private ArrayList<PatchChange> changes;

    public String getBuild() {
        return build;
    }

    protected void setBuild(String build) {
        this.build = build;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    protected void setDate(String date) {
        this.date = date;
    }

    public ArrayList<PatchChange> getChanges() {
        return changes;
    }

    public void setChanges(ArrayList<PatchChange> changes) {
        this.changes = changes;
    }
}
