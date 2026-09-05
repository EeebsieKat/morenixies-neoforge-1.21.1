package net.eeebsiekat.morenixies.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eeebsiekat.morenixies.content.NixieFlightHudBlock;
import net.eeebsiekat.morenixies.content.NixieFlightHudEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class NixieFlightHudRenderer implements BlockEntityRenderer<NixieFlightHudEntity> {

    private static final float LINE_THICKNESS = 0.004f;
    private static final ResourceLocation WHITE_TEX = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public NixieFlightHudRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(NixieFlightHudEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        NixieFlightHudEntity.DisplayMode mode = entity.getMode();
        if (mode == NixieFlightHudEntity.DisplayMode.OFF) return;

        Direction facing = entity.getBlockState().getValue(NixieFlightHudBlock.FACING);

        poseStack.pushPose();

        // 1. Align matrix to block center
        poseStack.translate(0.5F, 0.5F, 0.5F);

        // 2. Flipped 180° facing orientation to render on the correct outer face
        float yRot = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        // --- PANEL TYPE ROTATION (22.5° / 45° angled segments) ---
        float panelAngle = getPanelAngle(entity);
        if (panelAngle != 0.0f) {
            poseStack.mulPose(Axis.YP.rotationDegrees(panelAngle));
        }

        // 3. Dynamic X, Y, Z-offsets calculated per panel part
        float xOffset = getPanelXOffset(entity);
        float yOffset = getPanelYOffset(entity);
        float zOffset = getPanelZOffset(entity);

        poseStack.translate(xOffset, yOffset, zOffset);
        poseStack.scale(1.8f, 1.8f, 1.8f);

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(WHITE_TEX));
        Matrix4f staticMatrix = poseStack.last().pose();

        // --- RENDER BASED ON ASSIGNED DISPLAY MODE ---
        switch (mode) {
            case PITCH_ROLL -> {
                renderBoresightReticle(builder, staticMatrix, packedOverlay);

                float pitch = entity.getInterpolatedPitch(partialTick);
                float roll = entity.getInterpolatedRoll(partialTick);

                poseStack.pushPose();

                // FIX: Inverted roll angle so the horizon responds properly
                poseStack.mulPose(Axis.ZP.rotationDegrees(-roll));

                float pitchOffsetY = (pitch / 90.0f) * 0.35f;
                poseStack.translate(0.0F, -pitchOffsetY, 0.0F);

                Matrix4f dynamicMatrix = poseStack.last().pose();
                renderPitchLadder(builder, dynamicMatrix, packedOverlay);

                poseStack.popPose();
            }
            case SPEED -> {
                float speed = entity.getInterpolatedSpeed(partialTick);
                renderCenteredSpeedTape(builder, staticMatrix, speed, packedOverlay);
            }
            case ALTITUDE -> {
                float alt = entity.getInterpolatedAltitude(partialTick);
                renderAltitudeTape(builder, staticMatrix, alt, packedOverlay);
            }
            case HEADING -> {
                float yaw = entity.getInterpolatedYaw(partialTick);
                renderHeadingRibbon(builder, staticMatrix, yaw, packedOverlay);
            }
            case TANK_FULLNESS -> {
                renderTankGauge(builder, staticMatrix, packedOverlay);
            }
            default -> {}
        }

        poseStack.popPose();
    }

    /**
     * Determines local panel Y-axis tilt depending on panel position/type.
     */
    private float getPanelAngle(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.0f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END   ->  22.5f;
            case SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END -> -22.5f;
            case LARGE_SIDE_LEFT                        ->  45.0f;
            case LARGE_SIDE_RIGHT                       -> -45.0f;
            default                                     ->   0.0f;
        };
    }

    /**
     * Local panel X-offset (Left / Right shift)
     */
    private float getPanelXOffset(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.0f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END   ->  0.1f;
            case SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END ->  -0.1f;
            case LARGE_SIDE_LEFT                        ->  -0.09f;
            case LARGE_SIDE_RIGHT                       ->  0.09f;
            default                                     ->  0.0f;
        };
    }

    /**
     * Local panel Y-offset (Up / Down shift)
     */
    private float getPanelYOffset(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.0f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END,
                 SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END ->  0.2f;
            case LARGE_SIDE_LEFT, LARGE_SIDE_RIGHT       ->  0.2f;
            default                                       ->  0.2f;
        };
    }

    /**
     * Local panel Z-offset (Forward / Backward shift)
     */
    private float getPanelZOffset(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.05f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END,
                 SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END -> -0.25f;
            case LARGE_SIDE_LEFT, LARGE_SIDE_RIGHT       ->  0.3f;
            default                                       -> -0.45f;
        };
    }

    private void renderBoresightReticle(VertexConsumer builder, Matrix4f matrix, int overlay) {
        drawQuad(builder, matrix, -0.015f, -0.015f, 0.015f, 0.015f, overlay);
        drawQuad(builder, matrix, -0.08f, -0.003f, -0.03f, 0.003f, overlay);
        drawQuad(builder, matrix, -0.08f, -0.025f, -0.074f, 0.003f, overlay);
        drawQuad(builder, matrix, 0.03f, -0.003f, 0.08f, 0.003f, overlay);
        drawQuad(builder, matrix, 0.074f, -0.025f, 0.08f, 0.003f, overlay);
    }

    private void renderPitchLadder(VertexConsumer builder, Matrix4f matrix, int overlay) {
        drawQuad(builder, matrix, -0.25f, -0.002f, -0.08f, 0.002f, overlay);
        drawQuad(builder, matrix, 0.08f, -0.002f, 0.25f, 0.002f, overlay);

        for (int step = -30; step <= 30; step += 10) {
            if (step == 0) continue;

            float y = (step / 90.0f) * 0.35f;
            float hw = 0.06f;
            float drop = step > 0 ? -0.015f : 0.015f;

            // Left Ladder Rung
            drawQuad(builder, matrix, -hw - 0.05f, y - LINE_THICKNESS, -hw, y + LINE_THICKNESS, overlay);
            drawQuad(builder, matrix, -hw - 0.05f, y, -hw - 0.05f + (LINE_THICKNESS * 2), y + drop, overlay);

            // Right Ladder Rung
            drawQuad(builder, matrix, hw, y - LINE_THICKNESS, hw + 0.05f, y + LINE_THICKNESS, overlay);
            drawQuad(builder, matrix, hw + 0.05f - (LINE_THICKNESS * 2), y, hw + 0.05f, y + drop, overlay);
        }
    }

    private void renderCenteredSpeedTape(VertexConsumer builder, Matrix4f matrix, float speed, int overlay) {
        // --- 1. DIGITAL SPEED READOUT BOX (Outline Frame Only) ---
        float boxMinX = -0.09f, boxMaxX = 0.09f;
        float boxMinY = 0.08f,  boxMaxY = 0.16f;
        float border = 0.005f;

        // Outer border lines
        drawQuad(builder, matrix, boxMinX, boxMaxY - border, boxMaxX, boxMaxY, overlay); // Top
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMaxX, boxMinY + border, overlay); // Bottom
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMinX + border, boxMaxY, overlay); // Left
        drawQuad(builder, matrix, boxMaxX - border, boxMinY, boxMaxX, boxMaxY, overlay); // Right

        // --- 2. DIGITAL READOUT DIGITS ---
        int roundedSpeed = Math.min(999, Math.max(0, Math.round(speed)));
        int d100 = (roundedSpeed / 100) % 10;
        int d10  = (roundedSpeed / 10) % 10;
        int d1   = roundedSpeed % 10;

        renderDigit(builder, matrix, -0.055f, 0.095f, d100, overlay);
        renderDigit(builder, matrix, -0.015f, 0.095f, d10, overlay);
        renderDigit(builder, matrix,  0.025f, 0.095f, d1, overlay);

        // --- 3. UNITS INDICATOR (m/s) ---
        renderUnitMPS(builder, matrix, -0.03f, 0.035f, overlay);

        // --- 4. ANALOG SPEED TAPE LADDER ---
        float railTop = 0.01f;
        float railBottom = -0.19f;
        float railLeft = -0.12f;
        float railRight = 0.12f;

        // Vertical Side Rails
        drawQuad(builder, matrix, railLeft - LINE_THICKNESS, railBottom, railLeft, railTop, overlay);
        drawQuad(builder, matrix, railRight, railBottom, railRight + LINE_THICKNESS, railTop, overlay);

        // Static Center Pointers
        float centerY = (railTop + railBottom) / 2.0f; // -0.09f
        drawQuad(builder, matrix, railLeft - 0.03f, centerY - LINE_THICKNESS, railLeft, centerY + LINE_THICKNESS, overlay);
        drawQuad(builder, matrix, railRight, centerY - LINE_THICKNESS, railRight + 0.03f, centerY + LINE_THICKNESS, overlay);

        // Dynamic Moving Ticks
        float tickSpacing = 0.03f;
        float tapeOffset = (speed % 10.0f) / 10.0f * tickSpacing;

        for (int i = -3; i <= 3; i++) {
            float tickY = centerY + (i * tickSpacing) - tapeOffset;
            if (tickY >= railBottom && tickY <= railTop) {
                // Inner ticks
                drawQuad(builder, matrix, railLeft, tickY - (LINE_THICKNESS / 2f), railLeft + 0.02f, tickY + (LINE_THICKNESS / 2f), overlay);
                drawQuad(builder, matrix, railRight - 0.02f, tickY - (LINE_THICKNESS / 2f), railRight, tickY + (LINE_THICKNESS / 2f), overlay);
            }
        }
    }

    private void renderDigit(VertexConsumer builder, Matrix4f matrix, float x, float y, int digit, int overlay) {
        boolean a = (digit != 1 && digit != 4);
        boolean b = (digit != 5 && digit != 6);
        boolean c = (digit != 2);
        boolean d = (digit != 1 && digit != 4 && digit != 7);
        boolean e = (digit == 0 || digit == 2 || digit == 6 || digit == 8);
        boolean f = (digit != 1 && digit != 2 && digit != 3 && digit != 7);
        boolean g = (digit != 0 && digit != 1 && digit != 7);

        float w = 0.035f;
        float h = 0.06f;
        float t = 0.004f;

        if (a) drawQuad(builder, matrix, x, y + h - t, x + w, y + h, overlay);
        if (b) drawQuad(builder, matrix, x + w - t, y + (h / 2), x + w, y + h, overlay);
        if (c) drawQuad(builder, matrix, x + w - t, y, x + w, y + (h / 2), overlay);
        if (d) drawQuad(builder, matrix, x, y, x + w, y + t, overlay);
        if (e) drawQuad(builder, matrix, x, y, x + t, y + (h / 2), overlay);
        if (f) drawQuad(builder, matrix, x, y + (h / 2), x + t, y + h, overlay);
        if (g) drawQuad(builder, matrix, x, y + (h / 2) - (t / 2), x + w, y + (h / 2) + (t / 2), overlay);
    }

    private void renderUnitMPS(VertexConsumer builder, Matrix4f matrix, float x, float y, int overlay) {
        float t = 0.003f;
        drawQuad(builder, matrix, x, y, x + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x + 0.01f, y, x + 0.010f + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x + 0.02f, y, x + 0.020f + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x, y + 0.025f - t, x + 0.02f, y + 0.025f, overlay);

        drawQuad(builder, matrix, x + 0.03f, y, x + 0.036f, y + 0.03f, overlay);

        drawQuad(builder, matrix, x + 0.045f, y + 0.025f - t, x + 0.06f, y + 0.025f, overlay);
        drawQuad(builder, matrix, x + 0.045f, y + 0.012f, x + 0.045f + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x + 0.045f, y + 0.012f - (t / 2), x + 0.06f, y + 0.012f + (t / 2), overlay);
        drawQuad(builder, matrix, x + 0.06f - t, y, x + 0.06f, y + 0.012f, overlay);
        drawQuad(builder, matrix, x + 0.045f, y, x + 0.06f, y + t, overlay);
    }

    private void renderAltitudeTape(VertexConsumer builder, Matrix4f matrix, float alt, int overlay) {
        drawQuad(builder, matrix, -0.08f, -0.20f, 0.08f, 0.20f, overlay);
        drawQuad(builder, matrix, -0.07f, -0.19f, 0.07f, 0.19f, overlay);
    }

    private void renderHeadingRibbon(VertexConsumer builder, Matrix4f matrix, float yaw, int overlay) {
        drawQuad(builder, matrix, -0.20f, 0.10f, 0.20f, 0.15f, overlay);
    }

    private void renderTankGauge(VertexConsumer builder, Matrix4f matrix, int overlay) {
        drawQuad(builder, matrix, -0.10f, -0.20f, 0.10f, 0.20f, overlay);
    }

    private void drawQuad(VertexConsumer builder, Matrix4f matrix,
                          float minX, float minY, float maxX, float maxY, int overlay) {

        builder.addVertex(matrix, minX, minY, 0.0f).setColor(255, 160, 0, 220).setUv(0, 0).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
        builder.addVertex(matrix, maxX, minY, 0.0f).setColor(255, 160, 0, 220).setUv(1, 0).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
        builder.addVertex(matrix, maxX, maxY, 0.0f).setColor(255, 160, 0, 220).setUv(1, 1).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
        builder.addVertex(matrix, minX, maxY, 0.0f).setColor(255, 160, 0, 220).setUv(0, 1).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
    }
}