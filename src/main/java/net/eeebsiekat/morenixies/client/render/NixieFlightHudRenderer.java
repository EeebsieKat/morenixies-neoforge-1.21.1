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

                // Inverted roll angle so the horizon responds properly
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

    private float getPanelAngle(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.0f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END    ->  22.5f;
            case SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END -> -22.5f;
            case LARGE_SIDE_LEFT                        ->  45.0f;
            case LARGE_SIDE_RIGHT                       -> -45.0f;
            default                                     ->   0.0f;
        };
    }

    private float getPanelXOffset(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.0f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END    ->  0.1f;
            case SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END -> -0.1f;
            case LARGE_SIDE_LEFT                        -> -0.09f;
            case LARGE_SIDE_RIGHT                       ->  0.09f;
            default                                     ->  0.0f;
        };
    }

    private float getPanelYOffset(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.0f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END,
                 SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END -> 0.2f;
            case LARGE_SIDE_LEFT, LARGE_SIDE_RIGHT        -> 0.2f;
            default                                     -> 0.2f;
        };
    }

    private float getPanelZOffset(NixieFlightHudEntity entity) {
        var state = entity.getBlockState();

        if (!state.hasProperty(NixieFlightHudBlock.HUD_PART)) {
            return 0.05f;
        }

        return switch (state.getValue(NixieFlightHudBlock.HUD_PART)) {
            case SMALL_SIDE_LEFT, SMALL_SIDE_LEFT_END,
                 SMALL_SIDE_RIGHT, SMALL_SIDE_RIGHT_END -> -0.25f;
            case LARGE_SIDE_LEFT, LARGE_SIDE_RIGHT        ->  0.3f;
            default                                     -> -0.45f;
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
        // 1. DIGITAL SPEED READOUT BOX (Expanded to fit larger digits)
        float boxMinX = -0.11f, boxMaxX = 0.11f;
        float boxMinY = 0.07f,  boxMaxY = 0.17f;
        float border = 0.006f;

        // Outer border lines
        drawQuad(builder, matrix, boxMinX, boxMaxY - border, boxMaxX, boxMaxY, overlay); // Top
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMaxX, boxMinY + border, overlay); // Bottom
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMinX + border, boxMaxY, overlay); // Left
        drawQuad(builder, matrix, boxMaxX - border, boxMinY, boxMaxX, boxMaxY, overlay); // Right

        // 2. DIGITAL READOUT DIGITS
        int roundedSpeed = Math.min(999, Math.max(0, Math.round(speed)));
        int d100 = (roundedSpeed / 100) % 10;
        int d10  = (roundedSpeed / 10) % 10;
        int d1   = roundedSpeed % 10;

        float digitW = 0.050f;
        float digitH = 0.080f;
        float digitT = 0.008f; // Bolder stroke width

        renderDigit(builder, matrix, -0.085f, 0.080f, d100, digitW, digitH, digitT, overlay);
        renderDigit(builder, matrix, -0.025f, 0.080f, d10,  digitW, digitH, digitT, overlay);
        renderDigit(builder, matrix,  0.035f, 0.080f, d1,   digitW, digitH, digitT, overlay);

        // 3. UNITS INDICATOR (m/s)
        renderUnitMPS(builder, matrix, -0.03f, 0.025f, overlay);

        // 4. ANALOG SPEED TAPE LADDER
        float railTop = 0.00f;
        float railBottom = -0.19f;
        float railLeft = -0.12f;
        float railRight = 0.12f;

        // Vertical Side Rails
        drawQuad(builder, matrix, railLeft - LINE_THICKNESS, railBottom, railLeft, railTop, overlay);
        drawQuad(builder, matrix, railRight, railBottom, railRight + LINE_THICKNESS, railTop, overlay);

        // Static Center Pointers
        float centerY = (railTop + railBottom) / 2.0f;
        drawQuad(builder, matrix, railLeft - 0.03f, centerY - LINE_THICKNESS, railLeft, centerY + LINE_THICKNESS, overlay);
        drawQuad(builder, matrix, railRight, centerY - LINE_THICKNESS, railRight + 0.03f, centerY + LINE_THICKNESS, overlay);

        // Dynamic Moving Ticks
        float tickSpacing = 0.025f;
        float speedPerTick = 2.0f;
        float tapeOffset = (speed % speedPerTick) * (tickSpacing / speedPerTick);

        for (int i = -3; i <= 3; i++) {
            float tickY = centerY + (i * tickSpacing) - tapeOffset;
            if (tickY >= railBottom && tickY <= railTop) {
                drawQuad(builder, matrix, railLeft, tickY - (LINE_THICKNESS / 2f), railLeft + 0.02f, tickY + (LINE_THICKNESS / 2f), overlay);
                drawQuad(builder, matrix, railRight - 0.02f, tickY - (LINE_THICKNESS / 2f), railRight, tickY + (LINE_THICKNESS / 2f), overlay);
            }
        }
    }

    private void renderAltitudeTape(VertexConsumer builder, Matrix4f matrix, float alt, int overlay) {
        // 1. DIGITAL ALTITUDE READOUT BOX
        float boxMinX = -0.12f, boxMaxX = 0.12f;
        float boxMinY = 0.07f,  boxMaxY = 0.17f;
        float border = 0.006f;

        // Outer border lines
        drawQuad(builder, matrix, boxMinX, boxMaxY - border, boxMaxX, boxMaxY, overlay); // Top
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMaxX, boxMinY + border, overlay); // Bottom
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMinX + border, boxMaxY, overlay); // Left
        drawQuad(builder, matrix, boxMaxX - border, boxMinY, boxMaxX, boxMaxY, overlay); // Right

        // 2. DIGITAL READOUT DIGITS (-99 to 9999)
        int roundedAlt = Math.min(9999, Math.max(-99, Math.round(alt)));
        boolean isNegative = roundedAlt < 0;
        int absAlt = Math.abs(roundedAlt);

        int d1000 = (absAlt / 1000) % 10;
        int d100  = (absAlt / 100) % 10;
        int d10   = (absAlt / 10) % 10;
        int d1    = absAlt % 10;

        float digitW = 0.048f;
        float digitH = 0.080f;
        float digitT = 0.008f;

        if (isNegative) {
            // Negative: Render minus and 2 digits
            drawQuad(builder, matrix, -0.095f, 0.115f, -0.055f, 0.115f + digitT, overlay);
            renderDigit(builder, matrix, -0.045f, 0.080f, d10, digitW, digitH, digitT, overlay);
            renderDigit(builder, matrix,  0.010f, 0.080f, d1,  digitW, digitH, digitT, overlay);
        } else if (absAlt >= 1000) {
            // 4 Digits
            renderDigit(builder, matrix, -0.095f, 0.080f, d1000, digitW, digitH, digitT, overlay);
            renderDigit(builder, matrix, -0.045f, 0.080f, d100,  digitW, digitH, digitT, overlay);
            renderDigit(builder, matrix,  0.005f, 0.080f, d10,   digitW, digitH, digitT, overlay);
            renderDigit(builder, matrix,  0.055f, 0.080f, d1,    digitW, digitH, digitT, overlay);
        } else {
            // 3 Digits (Centered)
            renderDigit(builder, matrix, -0.070f, 0.080f, d100, digitW, digitH, digitT, overlay);
            renderDigit(builder, matrix, -0.010f, 0.080f, d10,  digitW, digitH, digitT, overlay);
            renderDigit(builder, matrix,  0.050f, 0.080f, d1,   digitW, digitH, digitT, overlay);
        }

        // 3. UNITS INDICATOR (ALT)
        renderUnitALT(builder, matrix, -0.021f, 0.025f, overlay);

        // 4. ANALOG ALTITUDE TAPE LADDER
        float railTop = 0.00f;
        float railBottom = -0.19f;
        float railLeft = -0.12f;
        float railRight = 0.12f;

        // Vertical Side Rails
        drawQuad(builder, matrix, railLeft - LINE_THICKNESS, railBottom, railLeft, railTop, overlay);
        drawQuad(builder, matrix, railRight, railBottom, railRight + LINE_THICKNESS, railTop, overlay);

        // Static Center Pointer Brackets
        float centerY = (railTop + railBottom) / 2.0f;
        drawQuad(builder, matrix, railLeft - 0.03f, centerY - LINE_THICKNESS, railLeft, centerY + LINE_THICKNESS, overlay);
        drawQuad(builder, matrix, railRight, centerY - LINE_THICKNESS, railRight + 0.03f, centerY + LINE_THICKNESS, overlay);

        // Dynamic Moving Ticks
        float tickSpacing = 0.03f;
        float altPerTick = 10.0f;
        float tapeOffset = (alt % altPerTick) * (tickSpacing / altPerTick);

        for (int i = -3; i <= 3; i++) {
            float tickY = centerY + (i * tickSpacing) - tapeOffset;
            if (tickY >= railBottom && tickY <= railTop) {
                drawQuad(builder, matrix, railLeft, tickY - (LINE_THICKNESS / 2f), railLeft + 0.03f, tickY + (LINE_THICKNESS / 2f), overlay);
                drawQuad(builder, matrix, railRight - 0.03f, tickY - (LINE_THICKNESS / 2f), railRight, tickY + (LINE_THICKNESS / 2f), overlay);
            }
        }
    }

    private void renderHeadingRibbon(VertexConsumer builder, Matrix4f matrix, float yaw, int overlay) {
        // 1. DIGITAL HEADING READOUT BOX
        float boxMinX = -0.12f, boxMaxX = 0.12f;
        float boxMinY = 0.07f,  boxMaxY = 0.17f;
        float border = 0.006f;

        // Outer border lines
        drawQuad(builder, matrix, boxMinX, boxMaxY - border, boxMaxX, boxMaxY, overlay); // Top
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMaxX, boxMinY + border, overlay); // Bottom
        drawQuad(builder, matrix, boxMinX, boxMinY, boxMinX + border, boxMaxY, overlay); // Left
        drawQuad(builder, matrix, boxMaxX - border, boxMinY, boxMaxX, boxMaxY, overlay); // Right

        // Normalize yaw to 0 - 360
        float normalizedYaw = (yaw % 360.0f + 360.0f) % 360.0f;

        // Determine cardinal direction string
        String headingStr = getCardinalDirection(normalizedYaw);

        // Render heading text inside the digital readout box
        renderHeadingText(builder, matrix, headingStr, overlay);

        // 2. HORIZONTAL HEADING TAPE (SCROLLING RIBBON) - Moved down
        float tapeMinX = -0.22f;
        float tapeMaxX = 0.22f;
        float tapeY = -0.16f;
        float tapeHeight = 0.10f;

        // Tape rails
        drawQuad(builder, matrix, tapeMinX, tapeY, tapeMaxX, tapeY + LINE_THICKNESS, overlay); // Bottom rail
        drawQuad(builder, matrix, tapeMinX, tapeY + tapeHeight, tapeMaxX, tapeY + tapeHeight + LINE_THICKNESS, overlay); // Top rail

        // Center pointer triangle / bracket
        float centerX = 0.0f;
        drawQuad(builder, matrix, centerX - LINE_THICKNESS, tapeY - 0.02f, centerX + LINE_THICKNESS, tapeY, overlay);

        // Dynamic Moving Scale Ticks & Directions (Every 15 degrees)
        float widthSpan = tapeMaxX - tapeMinX;

        for (int deg = 0; deg < 360; deg += 15) {
            float diff = deg - normalizedYaw;
            while (diff < -180f) diff += 360f;
            while (diff > 180f) diff -= 360f;

            // Visible within +/- 45 degrees from center
            if (diff >= -45.0f && diff <= 45.0f) {
                float posX = centerX + (diff / 45.0f) * (widthSpan / 2.0f);

                // Tick mark
                float tickH = (deg % 45 == 0) ? 0.03f : 0.015f;
                drawQuad(builder, matrix, posX - (LINE_THICKNESS / 2f), tapeY + tapeHeight, posX + (LINE_THICKNESS / 2f), tapeY + tapeHeight + tickH, overlay);

                // Cardinal labels on 45-degree intervals
                if (deg % 45 == 0) {
                    String cardinal = getCardinalForDegree(deg);
                    renderSmallCardinal(builder, matrix, posX - 0.015f, tapeY + tapeHeight + 0.035f, cardinal, overlay);
                }
            }
        }
    }

    private String getCardinalDirection(float yaw) {
        float y = (yaw % 360.0f + 360.0f) % 360.0f;
        if (y >= 337.5f || y < 22.5f) return "S";
        if (y >= 22.5f && y < 67.5f) return "SW";
        if (y >= 67.5f && y < 112.5f) return "W";
        if (y >= 112.5f && y < 157.5f) return "NW";
        if (y >= 157.5f && y < 202.5f) return "N";
        if (y >= 202.5f && y < 247.5f) return "NE";
        if (y >= 247.5f && y < 292.5f) return "E";
        return "SE";
    }

    private String getCardinalForDegree(int deg) {
        int normalized = (deg % 360 + 360) % 360;
        return switch (normalized) {
            case 0 -> "S";
            case 45 -> "SW";
            case 90 -> "W";
            case 135 -> "NW";
            case 180 -> "N";
            case 225 -> "NE";
            case 270 -> "E";
            case 315 -> "SE";
            default -> "";
        };
    }

    private void renderHeadingText(VertexConsumer builder, Matrix4f matrix, String text, int overlay) {
        float charW = 0.06f;
        float charH = 0.08f;
        float charT = 0.008f;

        if (text.length() == 1) {
            renderLetter(builder, matrix, text.charAt(0), -charW / 2.0f, 0.080f, charW, charH, charT, overlay);
        } else if (text.length() == 2) {
            float spacing = 0.01f;
            float totalWidth = (charW * 2) + spacing;
            float startX = -totalWidth / 2.0f;
            renderLetter(builder, matrix, text.charAt(0), startX, 0.080f, charW, charH, charT, overlay);
            renderLetter(builder, matrix, text.charAt(1), startX + charW + spacing, 0.080f, charW, charH, charT, overlay);
        }
    }

    private void renderSmallCardinal(VertexConsumer builder, Matrix4f matrix, float x, float y, String text, int overlay) {
        float charW = 0.025f;
        float charH = 0.035f;
        float charT = 0.004f;

        if (text.length() == 1) {
            renderLetter(builder, matrix, text.charAt(0), x, y, charW, charH, charT, overlay);
        } else if (text.length() == 2) {
            renderLetter(builder, matrix, text.charAt(0), x, y, charW * 0.8f, charH, charT, overlay);
            renderLetter(builder, matrix, text.charAt(1), x + charW * 0.8f + 0.002f, y, charW * 0.8f, charH, charT, overlay);
        }
    }

    private void renderLetter(VertexConsumer builder, Matrix4f matrix, char c, float x, float y, float w, float h, float t, int overlay) {
        switch (Character.toUpperCase(c)) {
            case 'N' -> {
                drawQuad(builder, matrix, x, y, x + t, y + h, overlay);
                drawQuad(builder, matrix, x + w - t, y, x + w, y + h, overlay);
                drawQuad(builder, matrix, x + t, y + h - t * 2, x + w - t, y + h, overlay);
                drawQuad(builder, matrix, x + t, y + h * 0.5f - t, x + w - t, y + h * 0.5f + t, overlay);
            }
            case 'S' -> {
                drawQuad(builder, matrix, x, y + h - t, x + w, y + h, overlay);
                drawQuad(builder, matrix, x, y + h / 2, x + t, y + h, overlay);
                drawQuad(builder, matrix, x, y + h / 2 - t / 2, x + w, y + h / 2 + t / 2, overlay);
                drawQuad(builder, matrix, x + w - t, y, x + w, y + h / 2, overlay);
                drawQuad(builder, matrix, x, y, x + w, y + t, overlay);
            }
            case 'E' -> {
                drawQuad(builder, matrix, x, y, x + t, y + h, overlay);
                drawQuad(builder, matrix, x, y + h - t, x + w, y + h, overlay);
                drawQuad(builder, matrix, x, y + h / 2 - t / 2, x + w - 0.01f, y + h / 2 + t / 2, overlay);
                drawQuad(builder, matrix, x, y, x + w, y + t, overlay);
            }
            case 'W' -> {
                drawQuad(builder, matrix, x, y, x + t, y + h, overlay);
                drawQuad(builder, matrix, x + w - t, y, x + w, y + h, overlay);
                drawQuad(builder, matrix, x + t, y, x + w - t, y + t + 0.01f, overlay);
                drawQuad(builder, matrix, x + w / 2 - t / 2, y + t, x + w / 2 + t / 2, y + h / 2, overlay);
            }
        }
    }

    private void renderDigit(VertexConsumer builder, Matrix4f matrix, float x, float y, int digit, float w, float h, float t, int overlay) {
        boolean a = (digit != 1 && digit != 4);
        boolean b = (digit != 5 && digit != 6);
        boolean c = (digit != 2);
        boolean d = (digit != 1 && digit != 4 && digit != 7);
        boolean e = (digit == 0 || digit == 2 || digit == 6 || digit == 8);
        boolean f = (digit != 1 && digit != 2 && digit != 3 && digit != 7);
        boolean g = (digit != 0 && digit != 1 && digit != 7);

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

    private void renderUnitALT(VertexConsumer builder, Matrix4f matrix, float x, float y, int overlay) {
        float t = 0.003f;
        // Letter 'A'
        drawQuad(builder, matrix, x, y, x + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x + 0.012f, y, x + 0.012f + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x, y + 0.025f - t, x + 0.012f + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x, y + 0.012f, x + 0.012f + t, y + 0.012f + t, overlay);

        // Letter 'L'
        drawQuad(builder, matrix, x + 0.018f, y, x + 0.018f + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x + 0.018f, y, x + 0.030f, y + t, overlay);

        // Letter 'T'
        drawQuad(builder, matrix, x + 0.036f, y, x + 0.036f + t, y + 0.025f, overlay);
        drawQuad(builder, matrix, x + 0.030f, y + 0.025f - t, x + 0.044f, y + 0.025f, overlay);
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