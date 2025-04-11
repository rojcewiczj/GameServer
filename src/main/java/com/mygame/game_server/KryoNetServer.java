package com.mygame.game_server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryo.Kryo;
import com.mygame.game_server.items.Arrow;
import com.mygame.game_server.models.NPC;
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
        kryo.register(Arrow.class);
        kryo.register(ArrowFiredCommand.class);
        kryo.register(ArrowData.class);
        kryo.register(MoveNPCCommand.class);
        kryo.register(NpcSnapshot.class);
    }

    private void startArrowUpdateLoop() {
        new Thread(() -> {
            while (true) {
                for (ArrowEntity arrow : world.arrows) {
                    arrow.update(world.trees);
                }
                world.arrows.removeIf(ArrowEntity::shouldDespawn);

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException ignored) {}
            }
        }, "ArrowUpdateLoop").start();
    }

    private void startWorldUpdateLoop() {
        new Thread(() -> {
            while (true) {
                float playerSpeed = 4f;
                float npcSpeed = 3f;

                // ✅ 0. Move players toward their targets with collision
                for (Player p : players.values()) {
                    float dx = p.targetX - p.x;
                    float dy = p.targetY - p.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dist > playerSpeed) {
                        float nextX = p.x + dx / dist * playerSpeed;
                        float nextY = p.y + dy / dist * playerSpeed;
                        if (!world.isBlocked(nextX, nextY)) {
                            p.x = nextX;
                            p.y = nextY;
                        }
                    } else {
                        p.x = p.targetX;
                        p.y = p.targetY;
                    }
                }

                // ✅ 0.5 Move NPCs toward their targets with collision
                for (NPC npc : world.npcs) {
                    float dx = npc.targetX - npc.x;
                    float dy = npc.targetY - npc.y;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dist > npcSpeed) {
                        float nextX = npc.x + dx / dist * npcSpeed;
                        float nextY = npc.y + dy / dist * npcSpeed;
                        if (!world.isBlocked(nextX, nextY)) {
                            npc.x = nextX;
                            npc.y = nextY;
                        }
                    } else {
                        npc.x = npc.targetX;
                        npc.y = npc.targetY;
                    }
                }

                // ✅ 1. Broadcast player snapshots to all
                WorldState state = new WorldState();
                List<PlayerSnapshot> snapshots = new ArrayList<>();
                for (Player p : players.values()) {
                    snapshots.add(new PlayerSnapshot(p.id, p.x, p.y));
                }
                state.players = snapshots;
                server.sendToAllTCP(state);

                // ✅ 2. Send per-player chunk updates (trees, arrows, npcs)
                for (Map.Entry<Connection, Player> entry : players.entrySet()) {
                    Connection conn = entry.getKey();
                    Player p = entry.getValue();

                    WorldChunkUpdate chunk = new WorldChunkUpdate();
                    chunk.trees = world.getTreesInView(p.x, p.y, 800, 600);
                    chunk.arrows = new ArrayList<>();
                    chunk.npcs = new ArrayList<>();

                    int halfW = 800 / 2;
                    int halfH = 600 / 2;
                    int minX = (int) p.x - halfW;
                    int minY = (int) p.y - halfH;
                    int maxX = (int) p.x + halfW;
                    int maxY = (int) p.y + halfH;

                    for (ArrowEntity arrow : world.arrows) {
                        if (arrow.x >= minX && arrow.x <= maxX && arrow.y >= minY && arrow.y <= maxY) {
                            chunk.arrows.add(new ArrowData(arrow.id, arrow.x, arrow.y, arrow.vx, arrow.vy));
                        }
                    }

                    for (NPC npc : world.npcs) {
                        if (npc.x >= minX && npc.x <= maxX && npc.y >= minY && npc.y <= maxY) {
                            chunk.npcs.add(new NpcSnapshot(npc.id, npc.x, npc.y));
                        }
                    }

                    conn.sendTCP(chunk);
                }

                try {
                    Thread.sleep(50); // ~20 FPS server update
                } catch (InterruptedException ignored) {}
            }
        }, "WorldChunkUpdateLoop").start();
    }

    @PostConstruct
    public void start() throws IOException {
        server.addListener(new ServerListener(players, nextPlayerId, world));
        server.bind(54555, 54777);
        server.start();
        System.out.println("✅ KryoNet Server started on ports 54555 (TCP), 54777 (UDP)");

        startArrowUpdateLoop();   // Run game logic smoothly
        startWorldUpdateLoop();   // Stream state at a stable rate
    }
}