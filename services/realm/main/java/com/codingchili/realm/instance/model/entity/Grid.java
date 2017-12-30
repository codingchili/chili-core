package com.codingchili.realm.instance.model.entity;


import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.context.Ticker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 * <p>
 * A grid to map entities onto a spatially hashed map.
 */
public class Grid {
    private volatile Map<Integer, List<Creature>> cells = new HashMap<>();
    private GameContext context;
    private int cellSize;
    private int gridWidth;

    /**
     * @param context  the context on which the grid is calculated.
     * @param cellSize the size of each bucket.
     */
    public Grid(GameContext context, int cellSize) {
        this.cellSize = cellSize;
        this.gridWidth = context.getInstance().settings().getWidth();
        this.context = context;

        context.ticker(this::update, 1);
    }

    private Grid update(Ticker ticker) {
        Collection<Creature> entities = context.getCreatures();
        Map<Integer, List<Creature>> buffer = new HashMap<>();
        entities.forEach(entity -> {
            entity.getVector().cells(cellSize, gridWidth).forEach(id -> {
                List<Creature> list = buffer.computeIfAbsent(id, key -> new ArrayList<>());
                list.add(entity);
            });
        });
        Map<Integer, List<Creature>> tmp = cells;
        cells = buffer;
        tmp.clear();
        return this;
    }

    /**
     * @param col the column to retrieve entities from.
     * @param row the row to retrieve entities from.
     * @return a list of entities in the given cell.
     */
    public List<Creature> list(int col, int row) {
        return cells.getOrDefault(col + row * gridWidth, Collections.emptyList());
    }

    /**
     * @param x the x position to get the cell of
     * @param y the y position to get the cell of
     * @return a point in the world converted to a cell.
     */
    public List<Creature> translate(int x, int y) {
        return cells.getOrDefault(x / cellSize + (y / cellSize) * gridWidth, Collections.emptyList());
    }

    /**
     * @param vector a vector that exists within a network partition.
     * @return a list of entities that exists in the same network partition.
     */
    public Collection<Creature> partition(Vector vector) {
        return context.getCreatures();
    }

    /**
     * Degree spread for cones.
     */
    private static final Integer DEGREES = 45;

    /**
     * @param vector contains a position which is the center of the cone and a direction which
     *               points the cone, vectors size is the radius length.
     * @return entities that exists within the cone.
     */
    public Set<Creature> cone(Vector vector) {
        return radius(vector).stream()
                .filter(entity -> {
                    Vector other = entity.getVector();
                    double deg = Math.toDegrees(
                            Math.atan2(other.getY() - vector.getY(),
                                    other.getX() - vector.getX()));

                    deg = (deg + 360) % 360;
                    return (vector.getDirection() - DEGREES <= deg && vector.getDirection() + 45 >= deg);

                }).collect(Collectors.toSet());
    }

    /**
     * @param vector contains the base position from which to expand a radius.
     *               The size of the vector is mapped to the length of the radius.
     * @return entities that exists within the given radius.
     */
    public Set<Creature> radius(Vector vector) {
        return adjacent(vector).stream().filter(entity -> {

            // check the distance from the given vector to entities in adjacent buckets.
            int distance = (int) Math.hypot(
                    entity.getVector().getX() - vector.getX(),
                    entity.getVector().getY() - vector.getY());

            // consider large entities.
            int max = vector.getSize() + entity.getVector().getSize();

            return (distance < max);
        }).collect(Collectors.toSet());
    }

    /**
     * @param vector a vector that exists in the grid, adjacent entities are selected.
     * @return adjacent entities to the given vector.
     */
    public Collection<Creature> adjacent(Vector vector) {
        Set<Creature> set = new HashSet<>();

        vector.cells(cellSize, gridWidth).forEach(bucket ->
                set.addAll(cells.getOrDefault(bucket, Collections.emptyList())));

        return set;
    }
}
