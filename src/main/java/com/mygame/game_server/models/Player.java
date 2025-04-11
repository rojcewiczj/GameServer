package com.mygame.game_server.models;

import com.mygame.game_server.items.Arrow;
import com.mygame.game_server.items.Axe;
import com.mygame.game_server.items.Bow;
import com.mygame.game_server.items.Item;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public int id;
    public String name;
    public float x = 0;
    public float y = 0;
    public float targetX = 0;
    public float targetY = 0;
    // ✅ Add inventory
    public final List<Item> inventory = new ArrayList<>();
    public boolean shootingMode = false;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;

        // ✅ Start with an axe for now
        inventory.add(new Axe(50));
        inventory.add(new Bow());
        for (int i = 0; i < 100; i++) {
            inventory.add(new Arrow());
        }
    }

    // Utility methods
    public boolean hasItem(Item item) {
        return inventory.contains(item);
    }

    public void addItem(Item item) {
        inventory.add(item);
    }
    public Axe getEquippedAxe() {
        for (Item item : inventory) {
            if (item instanceof Axe axe) return axe;
        }
        return null;
    }
    public boolean hasBow() {
        return inventory.stream().anyMatch(i -> i instanceof Bow);
    }

    public Bow getEquippedBow() {
        for (Item i : inventory) {
            if (i instanceof Bow bow) return bow;
        }
        return null;
    }
    public void removeItem(String item) {
        inventory.remove(item);
    }
    public boolean hasArrow() {
        return inventory.stream().anyMatch(i -> i instanceof Arrow);
    }

    public void removeOneArrow() {
        for (Item item : inventory) {
            if (item instanceof Arrow) {
                inventory.remove(item);
                return;
            }
        }
    }
}