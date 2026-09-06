package net.eeebsiekat.morenixies.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eeebsiekat.morenixies.content.NixieOscilloscopeEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;

public class NixieOscilloscopeRenderer implements BlockEntityRenderer<NixieOscilloscopeEntity> {

    private static final int TRACE_R = 255;
    private static final int TRACE_G = 125;
    private static final int TRACE_B = 15;
    private static final int TRACE_A = 240;

    private static final int CURSOR_R = 255;
    private static final int CURSOR_G = 220;
    private static final int CURSOR_B = 100;
    private static final int CURSOR_A = 255;

    private static final int GRID_R = 180;
    private static final int GRID_G = 60;
    private static final int GRID_B = 0;
    private static final int GRID_A = 90;

    private static final int FULL_BRIGHT = 0xF000F0;

    private static final float LINE_THICKNESS = 0.05f;
    private static final float GRID_THICKNESS = 0.015f;
    private static final float MAX_SPEED_MPS = 30f;

    // Offset graphics slightly in front of the block casing face (5.01 blocks out from center)
    private static final float FRONT_FACE_Z = 0.501f;

    private final Font font;

    public NixieOscilloscopeRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(NixieOscilloscopeEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!entity.isController()) return;

        Direction facing = entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        int totalWidth = entity.getScreenWidth();
        int totalHeight = entity.getScreenHeight();

        poseStack.pushPose();

        poseStack.translate(0.5F, 0.5F, 0.5F);

        float rotationAngle = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f; // NORTH
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

        float localOffsetX = (totalWidth - 1) * 0.5f;
        float localOffsetY = (totalHeight - 1) * 0.5f;
        poseStack.translate(localOffsetX, localOffsetY, FRONT_FACE_Z);

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(
                ResourceLocation.withDefaultNamespace("textures/misc/white.png")));
        Matrix4f matrix = poseStack.last().pose();

        float screenW = totalWidth - 0.1f;
        float screenH = totalHeight - 0.1f;

        renderGraticule(builder, matrix, screenW, screenH, packedOverlay);

        float[] history = entity.getHistoryBuffer();
        if (history != null && history.length > 1) {
            renderTrace(builder, matrix, history, screenW, screenH, packedOverlay);
        }

        renderCursor(builder, matrix, screenW, screenH, entity.getCurrentProgress(), packedOverlay);
        renderLabels(poseStack, buffer, screenW, screenH, entity.getTimeSpanSeconds());

        poseStack.popPose();
    }

    private void renderGraticule(VertexConsumer builder, Matrix4f matrix, float w, float h, int overlay) {
        float halfW = w / 2f;
        float halfH = h / 2f;
        float zPos = 0.001f;

        // Base Axis
        drawCustomLine(builder, matrix, -halfW, -halfH, zPos, halfW, -halfH, zPos, GRID_THICKNESS * 1.5f, GRID_R, GRID_G, GRID_B, GRID_A, overlay);

        // Speed Horizontal Divisions
        for (float factor = 0.25f; factor <= 1.0f; factor += 0.25f) {
            float yPos = -halfH + (h * factor);
            drawCustomLine(builder, matrix, -halfW, yPos, zPos, halfW, yPos, zPos, GRID_THICKNESS * 0.7f, GRID_R, GRID_G, GRID_B, GRID_A / 2, overlay);
        }

        // Time Vertical Divisions
        for (float factor = 0.25f; factor < 1.0f; factor += 0.25f) {
            float xPos = -halfW + (w * factor);
            drawCustomLine(builder, matrix, xPos, -halfH, zPos, xPos, halfH, zPos, GRID_THICKNESS * 0.5f, GRID_R, GRID_G, GRID_B, GRID_A / 2, overlay);
        }
    }

    private void renderTrace(VertexConsumer builder, Matrix4f matrix, float[] history, float w, float h, int overlay) {
        float startX = -w / 2f;
        float stepX = w / (history.length - 1);

        float bottomY = -h / 2f + 0.02f;
        float availableH = h - 0.04f;

        for (int i = 0; i < history.length - 1; i++) {
            float x1 = startX + (i * stepX);
            float x2 = startX + ((i + 1) * stepX);

            float normY1 = Mth.clamp(history[i] / MAX_SPEED_MPS, 0f, 1f);
            float normY2 = Mth.clamp(history[i + 1] / MAX_SPEED_MPS, 0f, 1f);

            float y1 = bottomY + (normY1 * availableH);
            float y2 = bottomY + (normY2 * availableH);

            // Glow Bloom Pass
            drawCustomLine(builder, matrix, x1, y1, 0.002f, x2, y2, 0.002f, LINE_THICKNESS * 2.2f, TRACE_R, 60, 0, 60, overlay);
            // Main Beam Core
            drawCustomLine(builder, matrix, x1, y1, 0.003f, x2, y2, 0.003f, LINE_THICKNESS, TRACE_R, TRACE_G, TRACE_B, TRACE_A, overlay);
        }
    }

    private void renderCursor(VertexConsumer builder, Matrix4f matrix, float w, float h, float progress, int overlay) {
        float halfW = w / 2f;
        float halfH = h / 2f;

        float cursorX = -halfW + (w * Mth.clamp(progress, 0f, 1f));

        drawCustomLine(builder, matrix, cursorX, -halfH, 0.004f, cursorX, halfH, 0.004f, GRID_THICKNESS * 1.5f, CURSOR_R, CURSOR_G, CURSOR_B, CURSOR_A, overlay);
    }

    private void renderLabels(PoseStack poseStack, MultiBufferSource buffer, float w, float h, float timeSpan) {
        float halfW = w / 2f;
        float halfH = h / 2f;
        float textScale = 0.008f;

        poseStack.pushPose();

        poseStack.translate(0, 0, 0.005f);
        poseStack.scale(textScale, -textScale, textScale);

        int textOrange = 0xFF960F;

        // X Axis labels
        font.drawInBatch("0s", (-halfW + 0.05f) / textScale, (-halfH + 0.08f) / -textScale, textOrange, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
        String endStr = ((int) timeSpan) + "s";
        font.drawInBatch(endStr, (halfW - 0.25f) / textScale, (-halfH + 0.08f) / -textScale, textOrange, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);

        // Y Axis labels
        font.drawInBatch((int) MAX_SPEED_MPS + " m/s", (-halfW + 0.05f) / textScale, (halfH - 0.15f) / -textScale, textOrange, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
        font.drawInBatch("0 m/s", (-halfW + 0.05f) / textScale, (-halfH + 0.2f) / -textScale, textOrange, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);

        poseStack.popPose();
    }

    private void drawCustomLine(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float thickness, int r, int g, int b, int a, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len == 0) return;

        float px = (-dy / len) * (thickness / 2.0f);
        float py = (dx / len) * (thickness / 2.0f);

        builder.addVertex(matrix, x1 - px, y1 - py, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 0, 1);
        builder.addVertex(matrix, x1 + px, y1 + py, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 0, 1);
        builder.addVertex(matrix, x2 + px, y2 + py, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 0, 1);
        builder.addVertex(matrix, x2 - px, y2 - py, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 0, 1);
    }
}