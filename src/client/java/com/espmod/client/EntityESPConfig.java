package com.espmod.client;

import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
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

public class EntityESPConfig {

    public static boolean enabled     = true;
    public static boolean showOutline = true;
    public static boolean showHitbox  = false;
    public static int     hitboxRange = 0; // 0 = use render distance
    public static List<EntityESPEntry> entities = new ArrayList<>();

    private static Map<EntityType<?>, EntityESPEntry> entityLookup = Map.of();

    public static Map<EntityType<?>, EntityESPEntry> getEntityLookup() { return entityLookup; }

    public static int getEffectiveHitboxRange() {
        if (hitboxRange > 0) return hitboxRange;
        Minecraft mc = Minecraft.getInstance();
        return mc != null ? Math.min(512, mc.options.renderDistance().get() * 16) : 64;
    }

    public static void rebuildLookup() {
        Map<EntityType<?>, EntityESPEntry> map = new HashMap<>();
        Set<String> ids = new HashSet<>();
        entities.removeIf(entry -> entry == null || entry.entityTypeId == null
            || Identifier.tryParse(entry.entityTypeId) == null);
        entities.forEach(entry -> entry.entityTypeId = Identifier.parse(entry.entityTypeId).toString());
        entities.removeIf(entry -> !ids.add(entry.entityTypeId));
        for (EntityESPEntry entry : entities) {
            BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(entry.entityTypeId))
                .ifPresent(type -> map.put(type, entry));
        }
        entityLookup = map;
    }

    public static void addEntity(EntityESPEntry entry) {
        entities.add(entry);
        rebuildLookup();
    }

    public static void removeEntity(String entityTypeId) {
        entities.removeIf(e -> e.entityTypeId.equals(entityTypeId));
        rebuildLookup();
    }

    public static void applyPreset(String category) {
        Set<String> ids = new HashSet<>();
        for (EntityESPEntry entry : entities) ids.add(entry.entityTypeId);
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            if (!categoryOf(t).equals(category)) continue;
            String id = EntityType.getKey(t).toString();
            if (!ids.add(id)) continue;
            entities.add(new EntityESPEntry(id, true, 0xFFFFFFFF));
        }
        rebuildLookup();
        save();
    }

    static String categoryOf(EntityType<?> t) {
        if (t == EntityTypes.PLAYER) return "player";
        if (!EntityType.getKey(t).getNamespace().equals("minecraft")) return "mods";
        if (t.getCategory() != MobCategory.MISC || isMiscMob(t)) return "mob";
        String p = keyPath(t);
        if (p.contains("boat") || p.contains("raft") || p.contains("minecart")) return "vehicle";
        if (p.contains("armor_stand") || p.contains("item_frame") ||
            p.contains("painting")    || p.contains("lead_knot"))  return "decorative";
        if (p.contains("arrow")        || p.contains("fireball")      ||
            p.contains("snowball")     || p.contains("egg")           ||
            p.contains("ender_pearl")  || p.contains("trident")       ||
            p.contains("potion")       || p.contains("firework")      ||
            p.contains("fishing")      || p.contains("shulker_bullet")||
            p.contains("wind_charge")  || p.contains("wither_skull"))  return "projectile";
        if (p.contains("item") || p.contains("experience") || p.contains("xp_orb")) return "items";
        return "other";
    }

    private static String keyPath(EntityType<?> t) {
        return EntityType.getKey(t).getPath();
    }

    private static boolean isMiscMob(EntityType<?> t) {
        String p = keyPath(t);
        return p.equals("villager") || p.equals("wandering_trader") ||
               p.equals("trader_llama") || p.contains("golem");
    }

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("espmod-entities.json");

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) { save(); return; }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Data data = ConfigFiles.GSON.fromJson(reader, Data.class);
            if (data != null) {
                enabled     = data.enabled;
                showOutline = data.showOutline;
                showHitbox  = data.showHitbox;
                hitboxRange = Math.clamp(data.hitboxRange, 0, 512);
                if (data.entities != null) entities = new ArrayList<>(data.entities);
            }
        } catch (IOException | JsonParseException e) { ConfigFiles.preserveInvalid(CONFIG_PATH, e); }
        rebuildLookup();
    }

    public static void save() {
        ConfigFiles.save(CONFIG_PATH, new Data());
    }

    private static class Data {
        boolean enabled     = EntityESPConfig.enabled;
        boolean showOutline = EntityESPConfig.showOutline;
        boolean showHitbox  = EntityESPConfig.showHitbox;
        int     hitboxRange = EntityESPConfig.hitboxRange;
        List<EntityESPEntry> entities = new ArrayList<>(EntityESPConfig.entities);
    }
}
