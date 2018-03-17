package com.codingchili.realm.instance.model.entity;


import com.codingchili.realm.instance.context.Ticker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 * <p>
 * A grid to map entities onto a spatially hashed map.
 */
public class Grid<T extends Entity> {
    private ConcurrentHashMap<String, T> entities = new ConcurrentHashMap<>();
    private volatile Map<Integer, List<T>> cells = new HashMap<>();
    private int cellSize = 256;
    private int gridWidth;

    /**
     * @param width the width of the grid
     */
    public Grid(int width) {
        this.gridWidth = width;
        this.cellSize = Math.max(width / 256, 256);
    }

    /**
     * Updates the entities in the grid.
     *
     * @param ticker the ticker that triggered the update.
     * @return fluent.
     */
    public Grid<T> update(Ticker ticker) {
        Map<Integer, List<T>> buffer = new HashMap<>();
        entities.values().forEach((entity) -> {
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
     * @param entity the entity to add to the grid.
     * @return fluent.
     */
    public Grid<T> add(T entity) {
        entities.put(entity.getId(), entity);
        return this;
    }

    /**
     * @param id the id of the entity to remove.
     * @return fluent.
     */
    public Grid<T> remove(String id) {
        entities.remove(id);
        return this;
    }

    /**
     * @param id the id of the entity to retrieve.
     * @return an entity with the given id.
     */
    public T get(String id) {
        T entity = entities.get(id);
        Objects.requireNonNull(entity, String.format("No entity with id '%s' found.", id));
        return entities.get(id);
    }

    /**
     * @param id the id to check if there is an entity registered for.
     * @return true if the entity exists on the grid.
     */
    public boolean exists(String id) {
        return entities.containsKey(id);
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
        return entities.values();
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
        // todo: partition entities into supercells for network updates.
        return entities.values();
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
