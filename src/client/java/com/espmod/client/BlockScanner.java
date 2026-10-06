package com.espmod.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import java.util.Collection;
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
        chunkFound.put(chunk.getPos(), matches);
    }

    public static void rescanAll() {
        chunkFound.clear();
        pending.clear();
        // ponytail: one chunk per tick; scan sections per tick if dense chunks cause measurable stalls.
        pending.addAll(loadedChunks.keySet());
    }

    public static void clear() {
        chunkFound.clear();
        loadedChunks.clear();
        pending.clear();
    }

    public static void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState) {
        Map<Block, Set<BlockPos>> matches = chunkFound.get(ChunkPos.containing(pos));
        if (matches == null) return; // A queued scan will read the updated chunk.
        Block oldBlock = oldState.getBlock();
        Set<BlockPos> oldPositions = matches.get(oldBlock);
        if (oldPositions != null) {
            oldPositions.remove(pos);
            if (oldPositions.isEmpty()) matches.remove(oldBlock);
        }
        Block newBlock = newState.getBlock();
        if (BlockESPConfig.getBlockLookup().containsKey(newBlock)) {
            matches.computeIfAbsent(newBlock, key -> new HashSet<>()).add(pos.immutable());
        }
    }

    public static Collection<Map<Block, Set<BlockPos>>> getFound() {
        return chunkFound.values();
    }
}
