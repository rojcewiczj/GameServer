package com.mygame.game_server.packets;

import com.mygame.game_server.GamePoint;

import java.awt.*;
import java.util.List;

public class WorldChunkUpdate {
    public List<GamePoint> trees;
    public List<ArrowData> arrows;
    public List<NpcSnapshot> npcs;
}