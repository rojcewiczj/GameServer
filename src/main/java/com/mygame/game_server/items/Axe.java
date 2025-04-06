package com.mygame.game_server.items;

public class Axe extends Item {
    public final float range;

    public Axe(float range) {
        super("axe");
        this.range = range;
    }
}