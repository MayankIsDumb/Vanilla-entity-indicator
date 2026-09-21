package com.invissee;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * 26.x renderer - draws outline on the block under every entity via
 * vanilla's gizmo system (Fabric BEFORE_GIZMOS event).
 * Depth-tested: boxes are occluded by walls, only visible in view.
 */
public class VisualizeEntitySupportingBlockRenderer {

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            if (!InvisSeeConfig.enabled) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            Vec3 playerPos = mc.player.position();
            double r = InvisSeeConfig.range;
            AABB searchBox = new AABB(playerPos.x - r, playerPos.y - r, playerPos.z - r,
                    playerPos.x + r, playerPos.y + r, playerPos.z + r);

            var entities = mc.level.getEntities((Entity) null, searchBox, e -> true);
            Set<BlockPos> rendered = new HashSet<>(entities.size() * 2);

            for (Entity entity : entities) {
                if (entity.isSpectator()) continue;
                if (!InvisSeeConfig.showInvisible && entity.isInvisible()) continue;
                if (InvisSeeConfig.onlyPlayers && !(entity instanceof Player)) continue;
                if (entity instanceof LivingEntity le && le.isDeadOrDying()) continue;

                BlockPos supportingPos = findSupportingBlockPos(entity, mc);
                if (supportingPos == null) continue;

                if (mc.level.getBlockState(supportingPos).isAir()) continue;

                if (!rendered.add(supportingPos.immutable())) continue;

                int color = InvisSeeConfig.getColorForPlayer(entity instanceof Player);
                try {
                    Gizmos.cuboid(supportingPos, GizmoStyle.stroke(color, InvisSeeConfig.lineWidth));
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private static BlockPos findSupportingBlockPos(Entity entity, Minecraft mc) {
        try {
            BlockPos onPos = entity.getOnPos();
            if (onPos != null && !mc.level.getBlockState(onPos).isAir()) {
                return onPos;
            }
        } catch (Exception ignored) {}

        double ex = entity.getX();
        double ey = entity.getY();
        double minY = entity.getBoundingBox().minY;

        for (int dy = 0; dy < 3; dy++) {
            BlockPos pos = BlockPos.containing(ex, minY - 0.1 - dy, entity.getZ());
            if (!mc.level.getBlockState(pos).isAir()) {
                return pos;
            }
        }
        BlockPos fallback = BlockPos.containing(ex, ey - 0.5, entity.getZ());
        if (!mc.level.getBlockState(fallback).isAir()) return fallback;
        return null;
    }
}
