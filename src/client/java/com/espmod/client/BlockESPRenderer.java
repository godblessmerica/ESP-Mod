package com.espmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.ChunkPos;
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
                for (var chunk : BlockScanner.getFound().entrySet()) {
                    if (!isChunkInRange(chunk.getKey(), px, pz, rSq)) continue;
                    for (Map.Entry<Block, Set<BlockPos>> e : chunk.getValue().entrySet()) {
                        BlockESPEntry entry = lookup.get(e.getKey());
                        if (entry == null || !entry.enabled) continue;
                        GizmoStyle style = GizmoStyle.stroke(entry.color, 1.5f);

                        for (BlockPos pos : e.getValue()) {
                            double dx = pos.getX() + 0.5 - px;
                            double dy = pos.getY() + 0.5 - py;
                            double dz = pos.getZ() + 0.5 - pz;
                            if (dx*dx + dy*dy + dz*dz > rSq) continue;
                            Gizmos.cuboid(new AABB(pos), style)
                                .setAlwaysOnTop();
                        }
                    }
                }
            }
        });
    }

    static boolean isChunkInRange(ChunkPos chunk, double x, double z, double rangeSquared) {
        // Use block centers, matching the per-block check; ignoring height keeps this conservative.
        double minX = chunk.getMinBlockX() + 0.5;
        double minZ = chunk.getMinBlockZ() + 0.5;
        double dx = Math.max(0, Math.max(minX - x, x - (minX + 15)));
        double dz = Math.max(0, Math.max(minZ - z, z - (minZ + 15)));
        return dx * dx + dz * dz <= rangeSquared;
    }
}
