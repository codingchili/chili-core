package com.codingchili.realm.instance.model.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Robin Duda
 * <p>
 * A vector within the game world.
 */
public class Vector {
    private float velocity = 1.0f;
    private float direction = 0.0f;
    private int size = 24;
    private float x = 0;
    private float y = 0;

    public float getX() {
        return x;
    }

    public Vector setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public Vector setY(float y) {
        this.y = y;
        return this;
    }

    public float getDirection() {
        return direction;
    }

    public Vector setDirection(float direction) {
        this.direction = direction;
        return this;
    }

    public float getVelocity() {
        return velocity;
    }

    public Vector setVelocity(float velocity) {
        this.velocity = velocity;
        return this;
    }

    public int getSize() {
        return size;
    }

    public Vector setSize(int size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        return String.format("x=%f y=%f, dir=%f velocity=%f", x, y, direction, velocity);
    }

    /**
     * @return a copy of this vector.
     */
    public Vector copy() {
        return new Vector()
                .setVelocity(velocity)
                .setDirection(direction)
                .setSize(size)
                .setX(x).setY(y);
    }


    /**
     * Returns the cells that the vector is placed in.
     *
     * @param cellSize  the size of the cells.
     * @param gridWidth the size of the grid.
     * @return cell numbers that this vector exists within.
     */
    public Set<Integer> cells(final int cellSize, final int gridWidth) {
        Set<Integer> buckets = new HashSet<>();
        buckets.add(Math.round(((x + size) / cellSize) + ((y / cellSize) * gridWidth)));
        buckets.add(Math.round(((x - size) / cellSize) + ((y / cellSize) * gridWidth)));
        buckets.add(Math.round((x / cellSize) + (((y + size) / cellSize) * gridWidth)));
        buckets.add(Math.round((x / cellSize) + (((y - size) / cellSize) * gridWidth)));
        return buckets;
    }

    /**
     * Moves the vector in its direction given its velocity.
     */
    public void forward() {
        // todo: support variable/configurable tick rate.
        x += Math.sin(direction) * velocity;
        y += Math.cos(direction) * velocity;
    }
}
