package com.codingchili.core.Realm.Configuration;

import com.codingchili.core.Realm.Model.Node;
import com.codingchili.core.Realm.Model.Npc;
import com.codingchili.core.Realm.Model.Portal;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Robin Duda
 *         Contains settings for an instance of a get.
 */
public class InstanceSettings implements Serializable {
    private ArrayList<Portal> portals;
    private ArrayList<Node> nodes;
    private ArrayList<Npc> npc;
    private String name;
    private int limit;
    private int width;
    private int height;

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public int getLimit() {
        return limit;
    }

    protected void setLimit(int limit) {
        this.limit = limit;
    }

    public int getWidth() {
        return width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public ArrayList<Portal> getPortals() {
        return portals;
    }

    protected void setPortals(ArrayList<Portal> portals) {
        this.portals = portals;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    protected void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Npc> getNpc() {
        return npc;
    }

    protected void setNpc(ArrayList<Npc> npc) {
        this.npc = npc;
    }
}
