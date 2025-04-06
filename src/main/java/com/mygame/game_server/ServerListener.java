package com.mygame.game_server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.mygame.game_server.models.Player;
import com.mygame.game_server.models.World;
import com.mygame.game_server.packets.ChopTreeCommand;
import com.mygame.game_server.packets.MoveCommand;
import com.mygame.game_server.packets.RegisterName;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerListener extends Listener {

    private final ConcurrentHashMap<Connection, Player> players;
    private final AtomicInteger idCounter;
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
        }

        if (object instanceof MoveCommand move) {
            Player player = players.get(connection);
            if (player != null) {
                player.x = move.x;
                player.y = move.y;
                System.out.printf("➡ %s moved to (%.1f, %.1f)%n", player.name, player.x, player.y);
            }
        }
        if (object instanceof ChopTreeCommand chop) {
            Player player = players.get(connection);
            if (player == null || !player.hasItem("axe")) {
                System.out.println("❌ Player tried to chop without an axe");
                return;
            }

            GamePoint toChop = null;
            for (GamePoint tree : world.trees) {
                if (Math.abs(tree.x - chop.x) < 5 && Math.abs(tree.y - chop.y) < 5) {
                    toChop = tree;
                    break;
                }
            }

            if (toChop != null) {
                world.trees.remove(toChop);
                player.addItem("wood");
                System.out.printf("🪓 %s chopped a tree at (%d, %d)%n", player.name, toChop.x, toChop.y);
            }
        }
    }
}