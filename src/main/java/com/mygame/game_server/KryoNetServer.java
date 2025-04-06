package com.mygame.game_server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryo.Kryo;
import com.mygame.game_server.models.Player;
import com.mygame.game_server.models.World;
import com.mygame.game_server.packets.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.esotericsoftware.kryonet.Connection;
import java.util.List;
import java.io.IOException;

@Component
public class KryoNetServer {

    private final ConcurrentHashMap<Connection, Player> players = new ConcurrentHashMap<>();
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);

    private final World world = new World();
    private final Server server;

    public KryoNetServer() {
        server = new Server();
        registerPackets(server.getKryo());
    }

    private void registerPackets(Kryo kryo) {
        kryo.register(RegisterName.class);
        kryo.register(MoveCommand.class);
        kryo.register(PlayerSnapshot.class);
        kryo.register(WorldState.class);
        kryo.register(WorldChunkUpdate.class);
        kryo.register(GamePoint.class);
        kryo.register(ArrayList.class);
        kryo.register(ChopTreeCommand.class);
    }
    private void startWorldUpdateLoop() {
        new Thread(() -> {
            while (true) {
                // 1. Send player positions using PlayerSnapshot
                WorldState state = new WorldState();
                List<PlayerSnapshot> snapshots = new ArrayList<>();

                for (Player p : players.values()) {
                    snapshots.add(new PlayerSnapshot(p.id, p.x, p.y));
                }

                state.players = snapshots;
                server.sendToAllTCP(state);

                for (Map.Entry<Connection, Player> entry : players.entrySet()) {
                    Connection conn = entry.getKey();
                    Player p = entry.getValue();

                    WorldChunkUpdate chunk = new WorldChunkUpdate();
                    chunk.trees = world.getTreesInView(p.x, p.y, 800, 600);
                    System.out.println("Sent " + chunk.trees.size() + " trees to player " + p.id);// 🎯 player's actual position
                    conn.sendTCP(chunk);
                }

                try {
                    Thread.sleep(1000); // send update every second
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }
    @PostConstruct
    public void start() throws IOException {
        server.addListener(new ServerListener(players, nextPlayerId, world));
        server.bind(54555, 54777);
        server.start();
        System.out.println("✅ KryoNet Server started on ports 54555 (TCP), 54777 (UDP)");
        startWorldUpdateLoop(); // <- add this
    }
}
