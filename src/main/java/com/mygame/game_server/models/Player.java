package com.mygame.game_server.models;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public int id;
    public String name;
    public float x = 0;
    public float y = 0;

    // ✅ Add inventory
    public final List<String> inventory = new ArrayList<>();

    public Player(int id, String name) {
        this.id = id;
        this.name = name;

        // ✅ Start with an axe for now
        inventory.add("axe");
    }

    // Utility methods
    public boolean hasItem(String item) {
        return inventory.contains(item);
    }

    public void addItem(String item) {
        inventory.add(item);
    }

    public void removeItem(String item) {
        inventory.remove(item);
    }
}