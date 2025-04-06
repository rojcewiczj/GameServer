package com.mygame.game_server.packets;

public class ChopTreeCommand {
    public int x;
    public int y;

    public ChopTreeCommand() {}
    public ChopTreeCommand(int x, int y) {
        this.x = x;
        this.y = y;
    }
}