package com.invissee;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import java.util.HashSet;
import java.util.Set;

/**
 * Fabric port of vanilla 1.21.10+ debug feature:
 * visualize_entity_supporting_block - added Dec 9 2025.
 * Shows outline on block directly under each entity, only when visible
 * (depth-tested, occluded by walls).
 */
public class VisualizeEntitySupportingBlockRenderer {

    public static void register() {
        // BEFORE_DEBUG_RENDER works for 1.21.11+ new world rendering pipeline
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            if (!InvisSeeConfig.enabled) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            PoseStack poseStack = context.matrices();
            if (poseStack == null) return;

            var consumers = context.consumers();
            if (consumers == null) return;

            Vec3 camPos = mc.gameRenderer.getMainCamera().position();

            // Depth-tested vanilla LINES - boxes are occluded by walls,
            // so they only show when the entity / block is in view.
            RenderType lines = net.minecraft.client.renderer.rendertype.RenderTypes.LINES;
            VertexConsumer consumer = consumers.getBuffer(lines);

            Matrix4f pose = poseStack.last().pose();

            // Iterate all entities in render distance
            Vec3 playerPos = mc.player.position();
            double r = InvisSeeConfig.range;
            AABB searchBox = new AABB(playerPos.x - r, playerPos.y - r, playerPos.z - r,
                                      playerPos.x + r, playerPos.y + r, playerPos.z + r);

            var entities = mc.level.getEntities((Entity) null, searchBox, e -> true);
            Set<BlockPos> rendered = new HashSet<>(entities.size() * 2);

            for (Entity entity : entities) {
                if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) {
                    // still show self? vanilla shows all, but skip if too close? Keep showing
                }
                if (entity.isSpectator()) continue;
                if (!InvisSeeConfig.showInvisible && entity.isInvisible()) continue;
                if (InvisSeeConfig.onlyPlayers && !(entity instanceof Player)) continue;
                if (entity instanceof LivingEntity le && le.isDeadOrDying()) continue;

                // Skip if too far (entity distance already filtered)
                // Find supporting block pos - mimic vanilla logic: block directly below entity's feet
                BlockPos supportingPos = findSupportingBlockPos(entity, mc);
                if (supportingPos == null) continue;

                // Skip air (should have solid block)
                if (mc.level.getBlockState(supportingPos).isAir()) continue;

                // Dedupe - multiple entities on same block (e.g. pig + cow same block in video)
                if (!rendered.add(supportingPos.immutable())) continue;

                // Frustum culling skipped for 1.21.11+ pipeline (getFrustum removed); searchBox already limits

                // Color - use separate player/entity colors from config
                int color = InvisSeeConfig.getColorForPlayer(entity instanceof Player);
                float a = ((color >> 24) & 0xFF) / 255f;
                float red = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                if (a < 0.01f) a = 1.0f;

                // Render box at supportingPos, offset by -camPos, using poseStack
                // Use WorldRenderer.renderLineBox alternative: manual AABB
                // Box is exactly 1x1x1 at block pos
                double x = supportingPos.getX() - camPos.x;
                double y = supportingPos.getY() - camPos.y;
                double z = supportingPos.getZ() - camPos.z;

                // Use LevelRenderer.renderLineBox helper if available, else manual
                // For compatibility, call our own drawBox
                drawBox(pose, consumer, x, y, z, x + 1, y + 1, z + 1, red, g, b, a);
            }
        });
    }

    private static BlockPos findSupportingBlockPos(Entity entity, Minecraft mc) {
        // Try vanilla's getOnPos / getBlockPosBelowThatAffectsMyMovement if available
        // Fallback: search downwards 1-2 blocks for solid
        try {
            // In Mojang 1.21.1, Entity has getOnPos() returning BlockPos with offset logic
            // Use reflection-like direct call - if method exists, use it
            BlockPos onPos = entity.getOnPos(); // 1.21.1 Mojang name is getOnPos or getOnPosLegacy
            if (onPos != null && !mc.level.getBlockState(onPos).isAir()) {
                return onPos;
            }
        } catch (Exception ignored) {}

        // Fallback: position at feet -0.1 + search down
        double ex = entity.getX();
        double ey = entity.getY(); // feet Y
        double ez = entity.getZ();
        // entity.getBoundingBox().minY is same as getY for most, but use it for offset
        double minY = entity.getBoundingBox().minY;

        for (int dy = 0; dy < 3; dy++) {
            BlockPos pos = BlockPos.containing(ex, minY - 0.1 - dy, ez);
            if (!mc.level.getBlockState(pos).isAir()) {
                // Check if block is solid supporting (not just air)
                // Could also check isSolidRender etc, but keep simple: any non-air counts
                return pos;
            }
            // Also check directly below entity.getOnPosLegacy offset? already tried
        }
        // Last resort: block at entity's integer position below
        BlockPos fallback = BlockPos.containing(ex, ey - 0.5, ez);
        if (!mc.level.getBlockState(fallback).isAir()) return fallback;
        return null;
    }

    // Manual box outline drawing - same as LevelRenderer.renderLineBox in vanilla
    private static void drawBox(Matrix4f pose, VertexConsumer consumer,
                                double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ,
                                float r, float g, float b, float a) {
        // 12 edges
        // bottom face
        line(pose, consumer, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        line(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        line(pose, consumer, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        line(pose, consumer, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
        // top face
        line(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        line(pose, consumer, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(pose, consumer, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
        // vertical edges
        line(pose, consumer, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        line(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        line(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void line(Matrix4f pose, VertexConsumer consumer,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float r, float g, float b, float a) {
        float dx = (float)(x2 - x1);
        float dy = (float)(y2 - y1);
        float dz = (float)(z2 - z1);
        float len = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len > 1e-6) {
            dx /= len; dy /= len; dz /= len;
        }
        // 1.21.11 LINES format requires LineWidth - set via VertexConsumer.setLineWidth (added in 1.21.11)
        float lw = InvisSeeConfig.lineWidth;
        consumer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(r,g,b,a).setNormal(dx, dy, dz).setLineWidth(lw);
        consumer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(r,g,b,a).setNormal(dx, dy, dz).setLineWidth(lw);
    }
}
