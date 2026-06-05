package com.espmod.client;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ESPConfig {

    public static boolean enabled = false;

    public static boolean playerESP    = false;
    public static boolean mobESP       = false;
    public static boolean vehicleESP    = false;
    public static boolean technicalESP  = false;
    public static boolean allEntityESP  = false;

    public static boolean showOutline = true;
    public static boolean showHitbox  = false;

    public static Map<String, EntitySettings> entityOverrides = new ConcurrentHashMap<>();

    public static void applyPlayerPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            if (t == EntityType.PLAYER) {
                entityOverrides.put(EntityType.getKey(t).toString(),
                    new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    private static String keyPath(EntityType<?> t) {
        String full = EntityType.getKey(t).toString();
        return full.contains(":") ? full.substring(full.indexOf(':') + 1) : full;
    }

    private static boolean isMiscMob(EntityType<?> t) {
        String p = keyPath(t);
        return p.equals("villager") || p.equals("wandering_trader") ||
               p.equals("iron_golem") || p.equals("snow_golem") ||
               p.equals("trader_llama");
    }

    public static void applyMobPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            if (t != EntityType.PLAYER && (t.getCategory() != MobCategory.MISC || isMiscMob(t))) {
                entityOverrides.put(EntityType.getKey(t).toString(),
                    new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    public static void applyVehiclePreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            String p = keyPath(t);
            if (p.contains("boat") || p.contains("raft") || p.contains("minecart")) {
                entityOverrides.put(EntityType.getKey(t).toString(),
                    new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    public static void applyTechnicalPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            String p = keyPath(t);
            if (p.contains("area_effect_cloud") || p.contains("marker")   ||
                p.contains("interaction")       || p.contains("_display") ||
                p.contains("lightning")         || p.contains("falling_block") ||
                p.contains("end_crystal")       || p.contains("end_gateway")) {
                entityOverrides.put(EntityType.getKey(t).toString(),
                    new EntitySettings(enabled, showOutline, showHitbox));
            }
        }
    }

    public static void applyAllEntitiesPreset(boolean enabled) {
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            boolean entityEnabled;
            if (enabled) {
                entityEnabled = true;
            } else {
                String p = keyPath(t);
                if (t == EntityType.PLAYER)
                    entityEnabled = playerESP;
                else if (t.getCategory() != MobCategory.MISC || isMiscMob(t))
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

    public static EntitySettings getOverride(Entity entity) {
        return entityOverrides.get(EntityType.getKey(entity.getType()).toString());
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("espmod.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
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
                if (data.entityOverrides != null)
                    entityOverrides = new ConcurrentHashMap<>(data.entityOverrides);
            }
        } catch (Exception e) {
            // Corrupted or unreadable config — reset to defaults
            playerESP = true;
            applyPlayerPreset(true);
            save();
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
