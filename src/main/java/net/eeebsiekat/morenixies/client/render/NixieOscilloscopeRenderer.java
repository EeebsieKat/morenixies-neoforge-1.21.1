package net.eeebsiekat.morenixies.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eeebsiekat.morenixies.content.NixieOscilloscopeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class NixieOscilloscopeRenderer implements BlockEntityRenderer<NixieOscilloscopeEntity> {

    // Nixie Tube Neon Orange Spectrum
    private static final int TRACE_R = 255;
    private static final int TRACE_G = 125;
    private static final int TRACE_B = 15;
    private static final int TRACE_A = 240;

    private static final int GRID_R = 180;
    private static final int GRID_G = 60;
    private static final int GRID_B = 0;
    private static final int GRID_A = 90;

    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float LINE_THICKNESS = 0.008f;

    public NixieOscilloscopeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(NixieOscilloscopeEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // Render only from the controller block to avoid duplicated overlapping screens
        if (!entity.isController()) return;

        Direction facing = entity.getBlockState().getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);

        int totalWidth = entity.getScreenWidth();
        int totalHeight = entity.getScreenHeight();

        poseStack.pushPose();

        // Center matrix relative to the entire connected array
        float originOffsetX = (totalWidth - 1) * 0.5f;
        float originOffsetY = -(totalHeight - 1) * 0.5f;

        poseStack.translate(0.5F + originOffsetX, 0.5F + originOffsetY, 0.5F);

        float rotationAngle = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f; // NORTH
        };
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotationAngle));
        poseStack.translate(0.0, 0.0, 0.51f);

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(
                ResourceLocation.withDefaultNamespace("textures/misc/white.png")));
        Matrix4f matrix = poseStack.last().pose();

        // Calculate dynamic dimensions for multi-block screen
        float screenW = totalWidth - 0.1f;
        float screenH = totalHeight - 0.1f;

        renderGraticule(builder, matrix, screenW, screenH, packedOverlay);

        float[] history = entity.getHistoryBuffer();
        renderTrace(builder, matrix, history, screenW, screenH, packedOverlay);

        poseStack.popPose();
    }

    private void renderGraticule(VertexConsumer builder, Matrix4f matrix, float w, float h, int overlay) {
        float halfW = w / 2f;
        float halfH = h / 2f;
        float gridThickness = 0.003f;

        // Main Crosshairs
        drawCustomLine(builder, matrix, -halfW, 0, 0, halfW, 0, 0, gridThickness, GRID_R, GRID_G, GRID_B, GRID_A, overlay);
        drawCustomLine(builder, matrix, 0, -halfH, 0, 0, halfH, 0, gridThickness, GRID_R, GRID_G, GRID_B, GRID_A, overlay);

        // Division Sub-Ticks along horizontal grid
        float step = 0.25f;
        for (float x = -halfW; x <= halfW; x += step) {
            drawCustomLine(builder, matrix, x, -0.05f, 0, x, 0.05f, 0, gridThickness, GRID_R, GRID_G, GRID_B, GRID_A, overlay);
        }
        for (float y = -halfH; y <= halfH; y += step) {
            drawCustomLine(builder, matrix, -0.05f, y, 0, 0.05f, y, 0, gridThickness, GRID_R, GRID_G, GRID_B, GRID_A, overlay);
        }
    }

    private void renderTrace(VertexConsumer builder, Matrix4f matrix, float[] history, float w, float h, int overlay) {
        float startX = -w / 2f;
        float stepX = w / (history.length - 1);
        float halfH = (h / 2f) - 0.05f;

        // Auto-scale trace Y-height relative to display area
        float maxVal = 16f; // Max baseline Redstone/Metric height
        float scaleY = halfH / maxVal;

        for (int i = 0; i < history.length - 1; i++) {
            float x1 = startX + (i * stepX);
            float x2 = startX + ((i + 1) * stepX);

            float y1 = Mth.clamp(history[i] * scaleY, -halfH, halfH);
            float y2 = Mth.clamp(history[i + 1] * scaleY, -halfH, halfH);

            // Glow core line
            drawCustomLine(builder, matrix, x1, y1, 0, x2, y2, 0, LINE_THICKNESS, TRACE_R, TRACE_G, TRACE_B, TRACE_A, overlay);

            // Outer phosphor bloom
            drawCustomLine(builder, matrix, x1, y1, -0.001f, x2, y2, -0.001f, LINE_THICKNESS * 2.2f, TRACE_R, 60, 0, 60, overlay);
        }
    }

    private void drawCustomLine(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float thickness, int r, int g, int b, int a, int overlay) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len == 0) return;

        float px = (-dy / len) * (thickness / 2.0f);
        float py = (dx / len) * (thickness / 2.0f);

        builder.addVertex(matrix, x1 - px, y1 - py, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, x1 + px, y1 + py, z1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 + px, y2 + py, z2).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
        builder.addVertex(matrix, x2 - px, y2 - py, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(FULL_BRIGHT).setNormal(0, 1, 0);
    }
}