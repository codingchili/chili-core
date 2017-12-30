package com.codingchili.realm.instance.model.entity;


import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.context.Ticker;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 * <p>
 * A grid to map entities onto a spatially hashed map.
 */
public class Grid<T extends Entity> {
    private volatile Map<Integer, List<T>> cells = new HashMap<>();
    private Supplier<Collection<T>> supplier;
    private int cellSize = 256;
    private int gridWidth;

    /**
     * @param width the width of the grid
     * @param supplier a supplier of entities.
     */
    public Grid(int width, Supplier<Collection<T>> supplier) {
        this.gridWidth = width;
        this.cellSize = Math.max(width / 256, 256);
        this.supplier = supplier;
    }

    public Grid update(Ticker ticker) {
        Collection<T> entities = supplier.get();
        Map<Integer, List<T>> buffer = new HashMap<>();
        entities.forEach(entity -> {
            entity.getVector().cells(cellSize, gridWidth).forEach(id -> {
                List<T> list = buffer.computeIfAbsent(id, key -> new ArrayList<>());
                list.add(entity);
            });
        });
        Map<Integer, List<T>> tmp = cells;
        cells = buffer;
        tmp.clear();
        return this;
    }

    /**
     * @param col the column to retrieve entities from.
     * @param row the row to retrieve entities from.
     * @return a list of entities in the given cell.
     */
    public Collection<T> list(int col, int row) {
        return cells.getOrDefault(col + row * gridWidth, Collections.emptyList());
    }

    /**
     * @return all entities in the grid.
     */
    public Collection<T> all() {
        return supplier.get();
    }

    /**
     * @param x the x position to get the cell of
     * @param y the y position to get the cell of
     * @return a point in the world converted to a cell.
     */
    public Collection<T> translate(int x, int y) {
        return cells.getOrDefault(x / cellSize + (y / cellSize) * gridWidth, Collections.emptyList());
    }

    /**
     * @param vector a vector that exists within a network partition.
     * @return a list of entities that exists in the same network partition.
     */
    public Collection<T> partition(Vector vector) {
        return supplier.get();
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
    public Set<T> cone(Vector vector) {
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
    public Set<T> radius(Vector vector) {
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
    public Collection<T> adjacent(Vector vector) {
        Set<T> set = new HashSet<>();

        vector.cells(cellSize, gridWidth).forEach(bucket ->
                set.addAll(cells.getOrDefault(bucket, Collections.emptyList())));

        return set;
    }
}
