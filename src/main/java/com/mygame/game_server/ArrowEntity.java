package com.mygame.game_server;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mygame.game_server.models.World.TREE_RADIUS;

public class ArrowEntity {
    public int id;
    public float x, y;
    private final float targetX, targetY;
    public final float vx, vy;
    private final float speed = 10f;
    private boolean stopped = false;
    private long spawnTime;
    private static final AtomicInteger nextId = new AtomicInteger(1);
    public ArrowEntity(float startX, float startY, float targetX, float targetY) {
        this.id = nextId.getAndIncrement();
        this.spawnTime = System.currentTimeMillis();
        this.x = startX;
        this.y = startY;
        this.targetX = targetX;
        this.targetY = targetY;

        float dx = targetX - startX;
        float dy = targetY - startY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        this.vx = dx / length * speed;
        this.vy = dy / length * speed;
    }

    public void update(List<GamePoint> trees) {
        if (stopped) return;

        // move
        x += vx;
        y += vy;

        // check for collision
        for (GamePoint tree : trees) {
            float dx = tree.x - x;
            float dy = tree.y - y;
            float distSq = dx * dx + dy * dy;
            if (distSq < TREE_RADIUS * TREE_RADIUS) {
                stopped = true;
                break;
            }
        }

        // check if reached target
        float dx = targetX - x;
        float dy = targetY - y;
        if (dx * dx + dy * dy < speed * speed) {
            x = targetX;
            y = targetY;
            stopped = true;
        }
    }

    public boolean isStopped() {
        return stopped;
    }
    public boolean shouldDespawn() {
        return isStopped() && System.currentTimeMillis() - spawnTime > 500000;
    }
}