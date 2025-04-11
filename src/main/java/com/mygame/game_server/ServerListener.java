package com.mygame.game_server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygame.game_server.items.Axe;
import com.mygame.game_server.items.Item;
import com.mygame.game_server.models.NPC;
import com.mygame.game_server.models.Player;
import com.mygame.game_server.models.World;
import com.mygame.game_server.packets.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerListener extends Listener {

    private final ConcurrentHashMap<Connection, Player> players;
    private final AtomicInteger idCounter;
    private final AtomicInteger nextNpcId = new AtomicInteger(1000);
    private final World world;
    public ServerListener(ConcurrentHashMap<Connection, Player> players, AtomicInteger idCounter, World world) {
        this.players = players;
        this.idCounter = idCounter;
        this.world = world;
    }

    @Override
    public void connected(Connection connection) {
        System.out.println("🔌 Player connected: " + connection.getID());
    }

    @Override
    public void disconnected(Connection connection) {
        Player player = players.remove(connection);
        if (player != null) {
            System.out.println("❌ Player disconnected: " + player.name);
        }
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof RegisterName registerName) {
            int newId = idCounter.getAndIncrement();
            Player newPlayer = new Player(newId, registerName.name);
            players.put(connection, newPlayer);
            System.out.printf("✅ Registered player %s (ID: %d)%n", newPlayer.name, newId);
            // 🌱 Spawn 5 NPCs near this player
            for (int i = 0; i < 5; i++) {
                float offsetX = newPlayer.x + (float)(Math.random() * 60 - 30);
                float offsetY = newPlayer.y + (float)(Math.random() * 60 - 30);
                NPC npc = new NPC(nextNpcId.getAndIncrement(), offsetX, offsetY);
                world.npcs.add(npc);
            }
        }

        if (object instanceof MoveCommand move) {
            Player player = players.get(connection);
            if (player != null) {
                player.targetX = move.x;
                player.targetY = move.y;
                // ✅ Don't update player.x/y here — let the server loop move and check collision smoothly
            }
        }
        if (object instanceof ArrowFiredCommand shot) {
            Player player = players.get(connection);
            if (player == null || !player.hasArrow()) return;

            player.removeOneArrow();

            float startX = player.x;
            float startY = player.y;

            ArrowEntity arrow = new ArrowEntity(startX, startY, shot.targetX, shot.targetY);
            world.arrows.add(arrow);

            System.out.printf("🏹 %s fired an arrow from (%.1f, %.1f) to (%.1f, %.1f)%n",
                    player.name, startX, startY, shot.targetX, shot.targetY);
        }
        if (object instanceof ChopTreeCommand chop) {
            Player player = players.get(connection);

            Axe axe = player.getEquippedAxe();
            if (axe == null) {
                System.out.println("❌ Player tried to chop without an axe");
                return;
            }

            // Check if player is in range of the tree
            float dx = player.x - chop.x;
            float dy = player.y - chop.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist > axe.range) {
                System.out.printf("❌ %s is too far to chop (%.1f units, axe range %.1f)%n", player.name, dist, axe.range);
                return;
            }

            // Find and remove the tree
            GamePoint toChop = null;
            for (GamePoint tree : world.trees) {
                if (Math.abs(tree.x - chop.x) < 5 && Math.abs(tree.y - chop.y) < 5) {
                    toChop = tree;
                    break;
                }
            }

            if (toChop != null) {
                world.trees.remove(toChop);
                player.inventory.add(new Item("wood"));
                System.out.printf("🪓 %s chopped a tree at (%d, %d)%n", player.name, toChop.x, toChop.y);
            }
        }
        if (object instanceof MoveNPCCommand moveNpc) {
            for (NPC npc : world.npcs) {
                if (npc.id == moveNpc.npcId) {
                    npc.targetX = moveNpc.targetX;
                    npc.targetY = moveNpc.targetY;
                    System.out.printf("🧍 NPC %d ordered to move to (%.1f, %.1f)%n", npc.id, npc.targetX, npc.targetY);
                    break;
                }
            }
        }
    }
}