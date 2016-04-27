package Game.Model;

import java.util.ArrayList;

/**
 * Created by Robin on 2016-04-27.
 */
public class InstanceSettings {
    private String name;
    private int limit;
    private int width;
    private int height;
    private ArrayList<Portal> portals;
    private ArrayList<Node> nodes;
    private ArrayList<Npc> npc;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ArrayList<Portal> getPortals() {
        return portals;
    }

    public void setPortals(ArrayList<Portal> portals) {
        this.portals = portals;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Npc> getNpc() {
        return npc;
    }

    public void setNpc(ArrayList<Npc> npc) {
        this.npc = npc;
    }
}
