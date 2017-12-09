package com.codingchili.realm.instance.model;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Robin Duda
 */
public class Grid {
    private volatile Map<Integer, List<Entity>> cells = new HashMap<>();
    private int cellSize;
    private int gridWidth;

    public Grid(int cellSize, int gridWidth) {
        this.cellSize = cellSize;
        this.gridWidth = gridWidth;
    }

    public Grid update(Collection<Entity> entities) {
        Map<Integer, List<Entity>> buffer = new HashMap<>();
        entities.forEach(entity -> {
            entity.getVector().cells(cellSize, gridWidth).forEach(id -> {
                List<Entity> list = buffer.computeIfAbsent(id, key -> new ArrayList<>());
                list.add(entity);
            });
        });
        Map<Integer, List<Entity>> tmp = cells;
        cells = buffer;
        tmp.clear();
        return this;
    }

    public List<Entity> list(int col, int row) {
        return cells.getOrDefault(col + row * gridWidth, Collections.emptyList());
    }

    public List<Entity> translate(int x, int y) {
        return cells.getOrDefault(x / cellSize + (y / cellSize) * gridWidth, Collections.emptyList());
    }

    // todo returns entities in the same network partition.
    public Collection<Entity> partition(Vector vector) {
        return Collections.emptyList();
    }

    // todo: network culling - less granular grid.
    // todo: cone selector
    // todo: map spell selector types to selectors in the grid

    public Collection<Entity> radius(Vector vector) {
        return adjacent(vector).stream().filter(entity -> {

            // check the distance from the given vector to entities in adjacent buckets.
            int distance = (int) Math.hypot(
                    entity.getVector().getX() - vector.getX(),
                    entity.getVector().getY() - vector.getY());

            // consider large entities.
            int max = vector.getRadius() + entity.getVector().getRadius();

            return (distance < max);
        }).collect(Collectors.toList());
    }

    public Collection<Entity> adjacent(Vector vector) {
        Set<Entity> set = new HashSet<>();

        vector.cells(cellSize, gridWidth).forEach(bucket ->
                set.addAll(cells.getOrDefault(bucket, Collections.emptyList())));

        return set;
    }
}
