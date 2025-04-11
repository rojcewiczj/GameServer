package com.mygame.game_server.packets;

public class ArrowData {
    public int id;
    public float x, y;
    public float vx, vy;

    public ArrowData() {}
    public ArrowData(int id, float x, float y, float vx, float vy) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }
}