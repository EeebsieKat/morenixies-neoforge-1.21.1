package net.eeebsiekat.morenixies.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eeebsiekat.morenixies.content.NixieFlightHudBlock;
import net.eeebsiekat.morenixies.content.NixieFlightHudEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class NixieFlightHudRenderer implements BlockEntityRenderer<NixieFlightHudEntity> {

    // Amber Glow Pixel Colors (RGBA)
    private static final int RED = 255;
    private static final int GREEN = 140;
    private static final int BLUE = 0;
    private static final int ALPHA = 230;

    // Full bright lightmap override for glowing phosphor effect
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float LINE_THICKNESS = 0.007f;

    public NixieFlightHudRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NixieFlightHudEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = entity.getBlockState().getValue(NixieFlightHudBlock.FACING);

        poseStack.pushPose();

        // Base Alignments
        poseStack.translate(0.5, 0.5, 0.5);

        float rotationAngle = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f;
        };
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationAngle));

        // Position on glass canopy
        poseStack.translate(0.0, 0.16, 0.0);
        poseStack.scale(2.2f, 2.2f, 2.2f);

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(
                ResourceLocation.withDefaultNamespace("textures/misc/white.png")));
        Matrix4f matrix = poseStack.last().pose();

        // 1. Static Boresight Reticle (-v-)
        renderAircraftSymbol(builder, matrix, packedOverlay);

        // Fetch Telemetry Data
        float pitch = entity.getInterpolatedPitch(partialTick);
        float roll = entity.getInterpolatedRoll(partialTick);
        float yaw = entity.getInterpolatedYaw(partialTick);
        float speed = entity.getInterpolatedSpeed(partialTick);
        float altitude = entity.getInterpolatedAltitude(partialTick);
        float vSpeed = entity.getInterpolatedVerticalSpeed(partialTick);

        // 2. Pitch Ladder & Dynamic Horizon Screen
        if (entity.isShowPitchLadder()) {
            poseStack.pushPose();
            // Roll rotation about center reticle
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-roll));
            Matrix4f pitchMatrix = poseStack.last().pose();

            renderPitchLadder(builder, pitchMatrix, pitch, packedOverlay);
            poseStack.popPose();
        }

        // 3. Side Tapes (Speed & Altitude/VSI) & Heading Ribbon
        if (entity.isShowTapes()) {
            renderSpeedTape(builder, matrix, speed, packedOverlay);
            renderAltitudeTape(builder, matrix, altitude, vSpeed, packedOverlay);
            renderHeadingRibbon(builder, matrix, yaw, packedOverlay);
        }

        poseStack.popPose();
    }

    private void renderAircraftSymbol(VertexConsumer builder, Matrix4f matrix, int overlay) {
        // Center reticle
        drawThickLine(builder, matrix, -0.08f, 0.0f, 0.0f, -0.03f, 0.0f, 0.0f, LINE_THICKNESS, overlay);
        drawThickLine(builder, matrix, -0.03f, 0.0f, 0.0f, 0.0f, -0.025f, 0.0f, LINE_THICKNESS, overlay);
        drawThickLine(builder, matrix, 0.0f, -0.025f, 0.0f, 0.03f, 0.0f, 0.0f, LINE_THICKNESS, overlay);
        drawThickLine(builder, matrix, 0.03f, 0.0f, 0.0f, 0.08f, 0.0f, 0.0f, LINE_THICKNESS, overlay);
    }

    private void renderPitchLadder(VertexConsumer builder, Matrix4f matrix, float pitch, int overlay) {
        // 1 deg pitch = 0.005 units shift
        float pitchShift = (pitch / 90.0f) * 0.35f;

        // Horizon Line
        float horizonY = -pitchShift;
        if (horizonY >= -0.2f && horizonY <= 0.2f) {
            drawThickLine(builder, matrix, -0.22f, horizonY, 0.0f, -0.08f, horizonY, 0.0f, LINE_THICKNESS, overlay);
            drawThickLine(builder, matrix, 0.08f, horizonY, 0.0f, 0.22f, horizonY, 0.0f, LINE_THICKNESS, overlay);
        }

        // +10 and -10 Pitch Rungs
        for (int step = -30; step <= 30; step += 10) {
            if (step == 0) continue;

            float rungY = ((step - pitch) / 90.0f) * 0.35f;
            if (rungY < -0.2f || rungY > 0.2f) continue;

            float halfWidth = 0.08f;
            float dropHeight = step > 0 ? -0.015f : 0.015f; // Angled tick marks

            // Left Rung
            drawThickLine(builder, matrix, -halfWidth - 0.04f, rungY, 0.0f, -halfWidth, rungY, 0.0f, LINE_THICKNESS, overlay);
            drawThickLine(builder, matrix, -halfWidth - 0.04f, rungY, 0.0f, -halfWidth - 0.04f, rungY + dropHeight, 0.0f, LINE_THICKNESS, overlay);

            // Right Rung
            drawThickLine(builder, matrix, halfWidth, rungY, 0.0f, halfWidth + 0.04f, rungY, 0.0f, LINE_THICKNESS, overlay);
            drawThickLine(builder, matrix, halfWidth + 0.04f, rungY, 0.0f, halfWidth + 0.04f, rungY + dropHeight, 0.0f, LINE_THICKNESS, overlay);
        }
    }

    private void renderSpeedTape(VertexConsumer builder, Matrix4f matrix, float speed, int overlay) {
        float x = -0.22f;
        // Vertical Rail
        drawThickLine(builder, matrix, x, -0.18f, 0.0f, x, 0.18f, 0.0f, LINE_THICKNESS, overlay);

        // Sliding Ticks
        float offset = (speed % 5.0f) * 0.01f;
        for (float y = -0.15f; y <= 0.15f; y += 0.05f) {
            float tickY = y - offset;
            if (tickY >= -0.18f && tickY <= 0.18f) {
                drawThickLine(builder, matrix, x - 0.02f, tickY, 0.0f, x, tickY, 0.0f, LINE_THICKNESS, overlay);
            }
        }
    }

    private void renderAltitudeTape(VertexConsumer builder, Matrix4f matrix, float altitude, float vSpeed, int overlay) {
        float x = 0.22f;
        // Vertical Rail
        drawThickLine(builder, matrix, x, -0.18f, 0.0f, x, 0.18f, 0.0f, LINE_THICKNESS, overlay);

        // Sliding Ticks
        float offset = (altitude % 10.0f) * 0.005f;
        for (float y = -0.15f; y <= 0.15f; y += 0.05f) {
            float tickY = y - offset;
            if (tickY >= -0.18f && tickY <= 0.18f) {
                drawThickLine(builder, matrix, x, tickY, 0.0f, x + 0.02f, tickY, 0.0f, LINE_THICKNESS, overlay);
            }
        }

        // Vertical Speed Indicator (VSI) Trend Arrow
        float vsiY = Mth.clamp(vSpeed * 0.02f, -0.15f, 0.15f);
        drawThickLine(builder, matrix, x + 0.03f, 0.0f, 0.0f, x + 0.03f, vsiY, 0.0f, LINE_THICKNESS * 1.2f, overlay);
    }

    private void renderHeadingRibbon(VertexConsumer builder, Matrix4f matrix, float yaw, int overlay) {
        float y = 0.19f;
        // Heading Rail
        drawThickLine(builder, matrix, -0.18f, y, 0.0f, 0.18f, y, 0.0f, LINE_THICKNESS, overlay);

        // Compass Center Notch
        drawThickLine(builder, matrix, 0.0f, y, 0.0f, 0.0f, y - 0.02f, 0.0f, LINE_THICKNESS * 1.5f, overlay);

        // Dynamic Moving Heading Ticks
        float offset = (yaw % 15.0f) * 0.004f;
        for (float x = -0.15f; x <= 0.15f; x += 0.04f) {
            float tickX = x - offset;
            if (tickX >= -0.18f && tickX <= 0.18f) {
                drawThickLine(builder, matrix, tickX, y, 0.0f, tickX, y + 0.015f, 0.0f, LINE_THICKNESS, overlay);
            }
        }
    }

    private void drawThickLine(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float thickness, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len == 0) return;

        float px = (-dy / len) * (thickness / 2.0f);
        float py = (dx / len) * (thickness / 2.0f);

        // Quad ribbon with FULL_BRIGHT lightmaps for self-illumination
        builder.addVertex(matrix, x1 - px, y1 - py, z1).setColor(RED, GREEN, BLUE, ALPHA).setUv(0, 0).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, x1 + px, y1 + py, z1).setColor(RED, GREEN, BLUE, ALPHA).setUv(0, 1).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 + px, y2 + py, z2).setColor(RED, GREEN, BLUE, ALPHA).setUv(1, 1).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 - px, y2 - py, z2).setColor(RED, GREEN, BLUE, ALPHA).setUv(1, 0).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
    }
}