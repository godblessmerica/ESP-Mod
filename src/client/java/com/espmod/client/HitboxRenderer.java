package com.espmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import java.util.Map;

public class HitboxRenderer {

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            if (!EntityESPConfig.enabled || !EntityESPConfig.showHitbox) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            Map<EntityType<?>, EntityESPEntry> lookup = EntityESPConfig.getEntityLookup();
            if (lookup.isEmpty()) return;

            double range = EntityESPConfig.getEffectiveHitboxRange();
            double rangeSquared = range * range;
            try (var collection = context.levelRenderer().collectPerFrameRenderThreadGizmos()) {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity == mc.player) continue;
                    if (entity.distanceToSqr(mc.player) > rangeSquared) continue;
                    EntityESPEntry entry = lookup.get(entity.getType());
                    if (entry == null || !entry.enabled) continue;
                    Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(entry.color, 2.5f)).setAlwaysOnTop();
                }
            }
        });
    }
}
