package com.mygame.game_server.models;

import com.mygame.game_server.items.Arrow;
import com.mygame.game_server.items.Axe;
import com.mygame.game_server.items.Bow;
import com.mygame.game_server.items.Item;

import java.util.ArrayList;
import java.util.List;

public class NPC {
    public int id;
    public float x, y;
    public String name;
    public float targetX, targetY;
    public List<Item> inventory = new ArrayList<>();
    public boolean shootingMode = false;
    public NPC(int id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;

        // Optionally start with an axe or other items
        inventory.add(new Axe(50));
        inventory.add(new Bow());
        for (int i = 0; i < 100; i++) {
            inventory.add(new Arrow());
        }
    }

    public void update(World world) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 4f) {
            float nextX = x + dx / dist * 4f;
            float nextY = y + dy / dist * 4f;
            if (!world.isBlocked(nextX, nextY)) {
                x = nextX;
                y = nextY;
            }
        }
    }
}
