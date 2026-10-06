package com.espmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import java.util.Map;
import java.util.Set;

public class BlockESPRenderer {

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            if (!BlockESPConfig.enabled) return;
            Map<Block, BlockESPEntry> lookup = BlockESPConfig.getBlockLookup();
            if (lookup.isEmpty()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            double r   = EntityESPConfig.getEffectiveHitboxRange();
            double rSq = r * r;
            double px  = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();

            try (var collection = context.levelRenderer().collectPerFrameRenderThreadGizmos()) {
                for (Map<Block, Set<BlockPos>> found : BlockScanner.getFound()) {
                    for (Map.Entry<Block, Set<BlockPos>> e : found.entrySet()) {
                        BlockESPEntry entry = lookup.get(e.getKey());
                        if (entry == null || !entry.enabled) continue;

                        for (BlockPos pos : e.getValue()) {
                            double dx = pos.getX() + 0.5 - px;
                            double dy = pos.getY() + 0.5 - py;
                            double dz = pos.getZ() + 0.5 - pz;
                            if (dx*dx + dy*dy + dz*dz > rSq) continue;
                            Gizmos.cuboid(new AABB(pos), GizmoStyle.stroke(entry.color, 1.5f))
                                .setAlwaysOnTop();
                        }
                    }
                }
            }
        });
    }
}
