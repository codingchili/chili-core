package com.codingchili.realm.instance.context;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.realm.instance.model.entity.Node;
import com.codingchili.realm.instance.model.npc.Npc;
import com.codingchili.realm.instance.model.entity.Portal;

import java.util.ArrayList;
import java.util.List;

import static com.codingchili.common.Strings.EXT_JSON;
import static com.codingchili.common.Strings.PATH_INSTANCE;

/**
 * @author Robin Duda
 * Contains settings for an instance in a realm.
 */
public class InstanceSettings implements Configurable {
    private List<Portal> portals = new ArrayList<>();
    private List<Node> nodes = new ArrayList<>();
    private List<Npc> npc = new ArrayList<>();
    private String name = "default";
    private int limit = 0;
    private int width = 1;
    private int height = 1;

    /**
     * @return the name of the realm.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name sets the name of the realm.
     * @return fluent
     */
    protected InstanceSettings setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the maximum number of players that may enter the instance.
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the maximum number of player that may enter the instance.
     * @return fluent
     */
    protected InstanceSettings setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * @return the width of the map.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width set the width of the map.
     * @return fluent
     */
    protected InstanceSettings setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * @return the height of the map.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height sets the height of the map.
     * @return fluent
     */
    protected InstanceSettings setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * @return a list of portals, exit points to other instances that exist.
     */
    public List<Portal> getPortals() {
        return portals;
    }

    /**
     * @param portals sets a list of portals that are exit points into other instances
     * @return fluent
     */
    protected InstanceSettings setPortals(List<Portal> portals) {
        this.portals = portals;
        return this;
    }

    /**
     * @param portal to add to the existing set of portals.
     * @return fluent
     */
    public InstanceSettings addPortal(Portal portal) {
        this.portals.add(portal);
        return this;
    }

    /**
     * @return a list of nodes that exists on the map.
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes a list of nodes to set.
     * @return fluent
     */
    protected InstanceSettings setNodes(List<Node> nodes) {
        this.nodes = nodes;
        return this;
    }

    /**
     * @param node a node on the map.
     * @return fluent
     */
    public InstanceSettings addNode(Node node) {
        this.nodes.add(node);
        return this;
    }

    /**
     * @return a list of npcs on the map.
     */
    public List<Npc> getNpc() {
        return npc;
    }

    /**
     * @param npc a list of npcs to set for the map.
     * @return fluent
     */
    protected InstanceSettings setNpc(List<Npc> npc) {
        this.npc = npc;
        return this;
    }

    /**
     * @param npc adds a npc to the list of existing.
     * @return fluent
     */
    public InstanceSettings addNpc(Npc npc) {
        this.npc.add(npc);
        return this;
    }

    @Override
    public String getPath() {
        return PATH_INSTANCE + name + EXT_JSON;
    }
}
