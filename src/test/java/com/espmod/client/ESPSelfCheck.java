package com.espmod.client;

import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ESPSelfCheck {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Path config = Files.createTempDirectory(Path.of("."), "config-").toAbsolutePath();
        var configDir = FabricLoaderImpl.class.getDeclaredField("configDir");
        configDir.setAccessible(true);
        configDir.set(FabricLoaderImpl.INSTANCE, config);
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        Path entityFile = config.resolve("espmod-entities.json");
        Files.writeString(entityFile, """
            {"hitboxRange":9999,"entities":[null,{},
              {"entityTypeId":"INVALID ID"},
              {"entityTypeId":"minecraft:player","enabled":true,"color":-1},
              {"entityTypeId":"player","enabled":false}]}
            """);
        EntityESPConfig.load();
        assert EntityESPConfig.hitboxRange == 512;
        assert EntityESPConfig.entities.size() == 1;
        assert EntityESPConfig.getEntityLookup().get(EntityTypes.PLAYER).enabled;
        EntityESPConfig.applyPreset("player");
        assert EntityESPConfig.entities.size() == 1;
        EntityESPConfig.hitboxRange = 0;
        EntityESPConfig.save();
        EntityESPConfig.load();
        assert EntityESPConfig.hitboxRange == 0;

        Path blockFile = config.resolve("blockesp.json");
        String invalid = "{ broken json";
        Files.writeString(blockFile, invalid);
        BlockESPConfig.load();
        assert Files.readString(blockFile).equals(invalid) : "Load must not overwrite invalid config";
        try (var backups = Files.list(config)) {
            assert backups.anyMatch(path -> path.getFileName().toString().startsWith("blockesp.json.invalid-"));
        }
        BlockESPConfig.blocks = new ArrayList<>();
        BlockESPConfig.addBlock(new BlockESPEntry("minecraft:diamond_ore", true, -1));
        BlockESPConfig.addBlock(new BlockESPEntry("diamond_ore", false, -1));
        assert BlockESPConfig.blocks.size() == 1;
        BlockESPConfig.addBlock(new BlockESPEntry("minecraft:gold_ore", true, -1));
        BlockESPConfig.addBlock(new BlockESPEntry("minecraft:deepslate_diamond_ore", true, -1));
        BlockESPConfig.save();
        BlockESPConfig.load();
        assert BlockESPConfig.getBlockLookup().size() == 3;

        // Seed a scanned chunk without creating a window or a client world.
        var cacheField = BlockScanner.class.getDeclaredField("chunkFound");
        cacheField.setAccessible(true);
        var cache = (Map<ChunkPos, Map<Block, Set<BlockPos>>>) cacheField.get(null);
        var loadedField = BlockScanner.class.getDeclaredField("loadedChunks");
        loadedField.setAccessible(true);
        var loaded = (Map<ChunkPos, ?>) loadedField.get(null);
        BlockPos pos = new BlockPos(-17, -60, 32);
        Map<Block, Set<BlockPos>> matches = new HashMap<>();
        matches.put(Blocks.DIAMOND_ORE, new HashSet<>(Set.of(pos)));
        cache.put(ChunkPos.containing(pos), matches);
        loaded.put(ChunkPos.containing(pos), null);
        BlockScanner.onBlockChanged(pos, Blocks.DIAMOND_ORE.defaultBlockState(), Blocks.GOLD_ORE.defaultBlockState());
        assert !matches.containsKey(Blocks.DIAMOND_ORE);
        assert matches.get(Blocks.GOLD_ORE).contains(pos);
        BlockScanner.onBlockChanged(pos, Blocks.GOLD_ORE.defaultBlockState(), Blocks.AIR.defaultBlockState());
        assert matches.isEmpty();
        // Loaded chunks awaiting their first scan must accept updates on both sides of a boundary.
        BlockPos left = new BlockPos(15, 64, 0);
        BlockPos right = new BlockPos(16, 64, 0);
        // Only loaded-chunk membership is needed; no client world is required for these updates.
        loaded.put(ChunkPos.containing(left), null);
        loaded.put(ChunkPos.containing(right), null);
        BlockScanner.onBlockChanged(left, Blocks.AIR.defaultBlockState(), Blocks.DIAMOND_ORE.defaultBlockState());
        BlockScanner.onBlockChanged(right, Blocks.AIR.defaultBlockState(), Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState());
        assert cache.get(ChunkPos.containing(left)) != null
            && cache.get(ChunkPos.containing(left)).get(Blocks.DIAMOND_ORE).contains(left)
            : "Placed diamond ore is lost while its chunk awaits scanning";
        assert cache.get(ChunkPos.containing(right)) != null
            && cache.get(ChunkPos.containing(right)).get(Blocks.DEEPSLATE_DIAMOND_ORE).contains(right)
            : "Placed ore disappears across a chunk boundary";
        BlockPos negative = new BlockPos(-1, 64, 0);
        loaded.put(ChunkPos.containing(negative), null);
        BlockScanner.onBlockChanged(negative, Blocks.AIR.defaultBlockState(), Blocks.DIAMOND_ORE.defaultBlockState());
        assert cache.get(ChunkPos.containing(negative)).get(Blocks.DIAMOND_ORE).contains(negative);
        loaded.put(new ChunkPos(100, 100), null);
        BlockScanner.rescanAll();
        assert cache.get(ChunkPos.containing(left)).get(Blocks.DIAMOND_ORE).contains(left)
            : "Changing a preset must not blank previously detected blocks";
        var pendingField = BlockScanner.class.getDeclaredField("pending");
        pendingField.setAccessible(true);
        var pending = (Set<ChunkPos>) pendingField.get(null);
        assert pending.iterator().next().equals(ChunkPos.ZERO) : "Nearby chunks must be rescanned first";
        BlockESPConfig.blocks.clear();
        BlockESPConfig.rebuildLookup();
        BlockScanner.rescanAll();
        assert BlockScanner.getFound().isEmpty() && pending.isEmpty();
        BlockScanner.clear();
        BlockESPConfig.addBlock(new BlockESPEntry("minecraft:diamond_ore", true, -1));
        BlockScanner.onBlockChanged(pos, Blocks.AIR.defaultBlockState(), Blocks.DIAMOND_ORE.defaultBlockState());
        BlockScanner.scanNext();
        assert BlockScanner.getFound().isEmpty() : "Cleared chunks must not reappear";
        assert !Blocks.OAK_STAIRS.defaultBlockState().isAir();
        System.out.println("ESP self-check passed: config safety, chunk-boundary updates, nearby rescans and cache reset.");
    }
}
