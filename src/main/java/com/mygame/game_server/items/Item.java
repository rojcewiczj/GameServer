package com.mygame.game_server.items;

public class Item {
    public final String name;

    public Item(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}