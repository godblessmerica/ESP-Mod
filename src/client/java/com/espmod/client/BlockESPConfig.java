package com.espmod.client;

import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BlockESPConfig {

    public static boolean enabled = false;
    public static List<BlockESPEntry> blocks = new ArrayList<>();

    private static Map<Block, BlockESPEntry> blockLookup = Map.of();

    public static Map<Block, BlockESPEntry> getBlockLookup() {
        return blockLookup;
    }

    public static void rebuildLookup() {
        Map<Block, BlockESPEntry> map = new HashMap<>();
        Set<String> ids = new HashSet<>();
        blocks.removeIf(entry -> entry == null || entry.blockId == null
            || Identifier.tryParse(entry.blockId) == null);
        blocks.forEach(entry -> entry.blockId = Identifier.parse(entry.blockId).toString());
        blocks.removeIf(entry -> !ids.add(entry.blockId));
        for (BlockESPEntry entry : blocks) {
            BuiltInRegistries.BLOCK.getOptional(Identifier.parse(entry.blockId))
                .ifPresent(block -> map.put(block, entry));
        }
        blockLookup = map;
    }

    public static void addBlock(BlockESPEntry entry) {
        blocks.add(entry);
        rebuildLookup();
    }

    public static void removeBlock(String blockId) {
        blocks.removeIf(e -> e.blockId.equals(blockId));
        rebuildLookup();
    }

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("blockesp.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Data data = ConfigFiles.GSON.fromJson(reader, Data.class);
            if (data != null) {
                enabled = data.enabled;
                if (data.blocks != null) blocks = new ArrayList<>(data.blocks);
            }
        } catch (IOException | JsonParseException e) {
            ConfigFiles.preserveInvalid(CONFIG_PATH, e);
        }
        rebuildLookup();
    }

    public static void save() {
        ConfigFiles.save(CONFIG_PATH, new Data());
    }

    private static class Data {
        boolean enabled = BlockESPConfig.enabled;
        List<BlockESPEntry> blocks = new ArrayList<>(BlockESPConfig.blocks);
    }
}
