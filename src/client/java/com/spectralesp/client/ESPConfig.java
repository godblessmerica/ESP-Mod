package com.spectralesp.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ESPConfig {

    public static boolean enabled = false;

    // UI state for the main menu preset buttons (all off by default)
    public static boolean playerESP    = false;
    public static boolean mobESP       = false;
    public static boolean vehicleESP    = false;
    public static boolean technicalESP  = false;
    public static boolean allEntityESP  = false;

    // Global display mode
    public static boolean showOutline = true;
    public static boolean showHitbox  = false;

    // Per-entity visibility — this is the ACTUAL source of truth for rendering
    public static Map<String, EntitySettings> entityOverrides = new HashMap<>();

    // ── Preset helpers ────────────────────────────────────────────────

    public static void applyPlayerPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            if (t == EntityType.PLAYER) {
                entityOverrides.put(EntityType.getKey(t).toString(),
                    new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    public static void applyMobPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            if (t != EntityType.PLAYER && t.getCategory() != MobCategory.MISC) {
                entityOverrides.put(EntityType.getKey(t).toString(),
                    new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    public static void applyVehiclePreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            String p = EntityType.getKey(t).toString();
            if (p.contains("boat") || p.contains("raft") || p.contains("minecart")) {
                entityOverrides.put(p, new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    public static void applyTechnicalPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            String p = EntityType.getKey(t).toString();
            if (p.contains("area_effect_cloud") || p.contains("marker")   ||
                p.contains("interaction")       || p.contains("_display") ||
                p.contains("lightning")         || p.contains("falling_block") ||
                p.contains("end_crystal")       || p.contains("end_gateway")) {
                entityOverrides.put(p, new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    public static void applyAllEntitiesPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            boolean entityEnabled;
            if (enabled) {
                entityEnabled = true;
            } else {
                // Turning all off: restore whatever preset flags say
                String p = EntityType.getKey(t).toString();
                if (t == EntityType.PLAYER)
                    entityEnabled = playerESP;
                else if (t.getCategory() != MobCategory.MISC)
                    entityEnabled = mobESP;
                else if (p.contains("boat") || p.contains("raft") || p.contains("minecart"))
                    entityEnabled = vehicleESP;
                else if (p.contains("area_effect_cloud") || p.contains("marker")   ||
                         p.contains("interaction")       || p.contains("_display") ||
                         p.contains("lightning")         || p.contains("falling_block") ||
                         p.contains("end_crystal")       || p.contains("end_gateway"))
                    entityEnabled = technicalESP;
                else
                    entityEnabled = false;
            }
            entityOverrides.put(EntityType.getKey(t).toString(),
                new EntitySettings(entityEnabled, showOutline, showHitbox));
        }
    }

    // ── Rendering lookup ──────────────────────────────────────────────

    public static EntitySettings getOverride(Entity entity) {
        return entityOverrides.get(EntityType.getKey(entity.getType()).toString());
    }

    // ── Persistence ───────────────────────────────────────────────────

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("spectral-esp.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            // First install: default to players shown, everything else off
            playerESP = true;
            applyPlayerPreset(true);
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data != null) {
                playerESP       = data.playerESP;
                mobESP          = data.mobESP;
                vehicleESP      = data.vehicleESP;
                technicalESP    = data.technicalESP;
                allEntityESP    = data.allEntityESP;
                showOutline     = data.showOutline;
                showHitbox      = data.showHitbox;
                if (data.entityOverrides != null) entityOverrides = data.entityOverrides;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(new Data(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Data {
        boolean playerESP    = ESPConfig.playerESP;
        boolean mobESP       = ESPConfig.mobESP;
        boolean vehicleESP    = ESPConfig.vehicleESP;
        boolean technicalESP  = ESPConfig.technicalESP;
        boolean allEntityESP  = ESPConfig.allEntityESP;
        boolean showOutline  = ESPConfig.showOutline;
        boolean showHitbox   = ESPConfig.showHitbox;
        Map<String, EntitySettings> entityOverrides = ESPConfig.entityOverrides;
    }
}
