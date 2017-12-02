package com.codingchili.realm.instance.model;

import java.util.*;

import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 */
public class Vector {
    private float velocity = 1.0f;
    private float direction = 0.0f;
    private int radius = 24;
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

    public int getRadius() {
        return radius;
    }

    public Vector setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public String toString() {
        return String.format("x=%f y=%f, dir=%f velocity=%f", x, y, direction, velocity);
    }

    public Set<Integer> buckets(final int cellSize, final int gridWidth) {
        Set<Integer> buckets = new TreeSet<>();
        buckets.add(Math.round(((x + radius) / cellSize) + ((y / cellSize) * gridWidth)));
        buckets.add(Math.round(((x - radius) / cellSize) + ((y / cellSize) * gridWidth)));
        buckets.add(Math.round((x / cellSize) + (((y + radius) / cellSize) * gridWidth)));
        buckets.add(Math.round((x / cellSize) + (((y - radius) / cellSize) * gridWidth)));
        return buckets;
    }
}
