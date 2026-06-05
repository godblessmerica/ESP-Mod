package com.espmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class HitboxRenderer {

    private static final int COLOR_PLAYER = 0xFFFF5555;
    private static final int COLOR_MOB    = 0xFFFFAA00;
    private static final int COLOR_ENTITY = 0xFFFFFF55;

    public static void register() {
        // END_EXTRACTION is when the per-frame gizmo collector is active.
        // Gizmos use world-space coordinates — no camera offset needed.
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            if (!ESPConfig.enabled || !ESPConfig.showHitbox) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            try (var collection = context.levelRenderer().collectPerFrameGizmos()) {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity == mc.player) continue;

                    EntitySettings ov = ESPConfig.getOverride(entity);
                    if (ov == null || !ov.enabled) continue;

                    int color;
                    if (entity instanceof Player)   color = COLOR_PLAYER;
                    else if (entity instanceof Mob) color = COLOR_MOB;
                    else                            color = COLOR_ENTITY;

                    Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(color, 2.5f)).setAlwaysOnTop();
                }
            }
        });
    }
}
