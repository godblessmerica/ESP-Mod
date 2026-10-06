package com.espmod.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class BlockScanner {
    // Chunk access, cache updates and gizmo submission all run on the client thread.
    private static final Map<ChunkPos, LevelChunk> loadedChunks = new HashMap<>();
    private static final Map<ChunkPos, Map<Block, Set<BlockPos>>> chunkFound = new HashMap<>();
    private static final Set<ChunkPos> pending = new LinkedHashSet<>();

    public static void register() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            loadedChunks.put(chunk.getPos(), chunk);
            chunkFound.remove(chunk.getPos());
            pending.add(chunk.getPos());
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            loadedChunks.remove(chunk.getPos());
            chunkFound.remove(chunk.getPos());
            pending.remove(chunk.getPos());
        });
        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((mc, level) -> clear());
        ClientTickEvents.END_CLIENT_TICK.register(mc -> scanNext());
    }

    static void scanNext() {
        if (pending.isEmpty()) return;
        ChunkPos pos = pending.iterator().next();
        pending.remove(pos);
        LevelChunk chunk = loadedChunks.get(pos);
        if (chunk != null) scanChunk(chunk);
    }

    private static void scanChunk(LevelChunk chunk) {
        Set<Block> tracked = BlockESPConfig.getBlockLookup().keySet();
        Map<Block, Set<BlockPos>> matches = new HashMap<>();
        if (!tracked.isEmpty()) {
            LevelChunkSection[] sections = chunk.getSections();
            int minX = chunk.getPos().getMinBlockX();
            int minZ = chunk.getPos().getMinBlockZ();
            for (int i = 0; i < sections.length; i++) {
                LevelChunkSection section = sections[i];
                if (section == null || section.hasOnlyAir()
                    || !section.maybeHas(state -> tracked.contains(state.getBlock()))) continue;
                int baseY = chunk.getMinY() + i * 16;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 16; y++) {
                            Block block = section.getBlockState(x, y, z).getBlock();
                            if (tracked.contains(block)) {
                                matches.computeIfAbsent(block, key -> new HashSet<>())
                                    .add(new BlockPos(minX + x, baseY + y, minZ + z));
                            }
                        }
                    }
                }
            }
        }
        if (matches.isEmpty()) chunkFound.remove(chunk.getPos());
        else chunkFound.put(chunk.getPos(), matches);
    }

    public static void rescanAll() {
        Set<Block> tracked = BlockESPConfig.getBlockLookup().keySet();
        chunkFound.values().forEach(matches -> matches.keySet().retainAll(tracked));
        chunkFound.values().removeIf(Map::isEmpty);
        pending.clear();
        if (tracked.isEmpty()) {
            chunkFound.clear();
            return;
        }
        Minecraft client = Minecraft.getInstance();
        ChunkPos center = client != null && client.player != null ? client.player.chunkPosition() : ChunkPos.ZERO;
        // ponytail: one chunk per tick; scan sections per tick if dense chunks cause measurable stalls.
        loadedChunks.keySet().stream().sorted(Comparator.comparingInt(pos -> pos.distanceSquared(center)))
            .forEach(pending::add);
    }

    public static void clear() {
        chunkFound.clear();
        loadedChunks.clear();
        pending.clear();
    }

    public static void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState) {
        ChunkPos chunkPos = ChunkPos.containing(pos);
        if (!loadedChunks.containsKey(chunkPos)) return;
        Map<Block, Set<BlockPos>> matches = chunkFound.get(chunkPos);
        Block newBlock = newState.getBlock();
        boolean tracked = BlockESPConfig.getBlockLookup().containsKey(newBlock);
        if (matches == null) {
            if (!tracked) return;
            matches = new HashMap<>();
            chunkFound.put(chunkPos, matches);
        }
        Block oldBlock = oldState.getBlock();
        Set<BlockPos> oldPositions = matches.get(oldBlock);
        if (oldPositions != null) {
            oldPositions.remove(pos);
            if (oldPositions.isEmpty()) matches.remove(oldBlock);
        }
        if (tracked) {
            matches.computeIfAbsent(newBlock, key -> new HashSet<>()).add(pos.immutable());
        }
        if (matches.isEmpty()) chunkFound.remove(chunkPos);
    }

    public static Map<ChunkPos, Map<Block, Set<BlockPos>>> getFound() {
        return chunkFound;
    }
}
