package com.mygame.game_server.models;

import com.mygame.game_server.ArrowEntity;
import com.mygame.game_server.GamePoint;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {
    public final int width = 2000;
    public final int height = 2000;
    public final List<GamePoint> trees = new ArrayList<>();
    public final List<ArrowEntity> arrows = new ArrayList<>();
    public List<NPC> npcs = new ArrayList<>();
    public static final float TREE_RADIUS = 16f;
    public static final float PLAYER_RADIUS = 12f;
    public World() {
        Random rand = new Random();
        // Generate clusters
        for (int cluster = 0; cluster < 10; cluster++) {
            int centerX = rand.nextInt(width);
            int centerY = rand.nextInt(height);

            int clusterSize = rand.nextInt(20) + 10;

            for (int i = 0; i < clusterSize; i++) {
                int offsetX = rand.nextInt(100) - 50;
                int offsetY = rand.nextInt(100) - 50;

                int treeX = Math.min(Math.max(centerX + offsetX, 0), width);
                int treeY = Math.min(Math.max(centerY + offsetY, 0), height);

                trees.add(new GamePoint(treeX, treeY));
            }
        }

        // Sprinkle random lone trees
        for (int i = 0; i < 100; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            trees.add(new GamePoint(x, y));
        }
    }
    public boolean isBlocked(float x, float y) {
        for (GamePoint tree : trees) {
            float dx = tree.x - x;
            float dy = tree.y - y;
            float distSq = dx * dx + dy * dy;
            float collisionDist = TREE_RADIUS + PLAYER_RADIUS;
            if (distSq < collisionDist * collisionDist) {
                return true;
            }
        }
        return false;
    }
    public List<GamePoint> getTreesInView(float x, float y, int viewWidth, int viewHeight) {
        List<GamePoint> visible = new ArrayList<>();
        int halfW = viewWidth / 2;
        int halfH = viewHeight / 2;
        int minX = (int) x - halfW;
        int minY = (int) y - halfH;
        int maxX = (int) x + halfW;
        int maxY = (int) y + halfH;

        for (GamePoint p : trees) {
            if (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY) {
                visible.add(p);
            }
        }

        return visible;
    }
}