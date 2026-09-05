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
        Direction facing = entity.getBlockState().getValue(NixieFlightHudBlock.FACING);

        poseStack.pushPose();

        // 1. Align matrix to block center
        poseStack.translate(0.5F, 0.5F, 0.5F);

        // 2. Orient matrix to face inward toward the pilot
        float yRot = switch (facing) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case EAST -> 270f;
            default -> 180f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        // 3. Offsets: Centered on X (0.0F), lowered Y (0.15F), front concave surface (-0.05F Z)
        poseStack.translate(0.0F, 0.15F, -0.05F);
        poseStack.scale(1.8f, 1.8f, 1.8f);

        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(WHITE_TEX));
        Matrix4f staticMatrix = poseStack.last().pose();

        // --- STATIC HUD ELEMENTS (Screen Fixed) ---
        renderBoresightReticle(builder, staticMatrix, packedOverlay);

        if (entity.isShowTapes()) {
            float speed = entity.getInterpolatedSpeed(partialTick);
            float alt = entity.getInterpolatedAltitude(partialTick);
            float yaw = entity.getInterpolatedYaw(partialTick);

            renderSpeedTape(builder, staticMatrix, speed, packedOverlay);
            renderAltitudeTape(builder, staticMatrix, alt, packedOverlay);
            renderHeadingRibbon(builder, staticMatrix, yaw, packedOverlay);
        }

        // --- DYNAMIC HUD ELEMENTS ---
        if (entity.isShowPitchLadder()) {
            float pitch = entity.getInterpolatedPitch(partialTick);
            float roll = entity.getInterpolatedRoll(partialTick);

            poseStack.pushPose();

            // Inverted roll angle sign to compensate for flipped Y-axis facing orientation
            poseStack.mulPose(Axis.ZP.rotationDegrees(-roll));

            // Pitch offset along vertical axis
            float pitchOffsetY = (pitch / 90.0f) * 0.35f;
            poseStack.translate(0.0F, -pitchOffsetY, 0.0F);

            Matrix4f dynamicMatrix = poseStack.last().pose();
            renderPitchLadder(builder, dynamicMatrix, packedOverlay);

            poseStack.popPose();
        }

        poseStack.popPose();
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

    private void renderSpeedTape(VertexConsumer builder, Matrix4f matrix, float speed, int overlay) {
        drawQuad(builder, matrix, -0.28f, -0.20f, -0.27f, 0.20f, overlay);
    }

    private void renderAltitudeTape(VertexConsumer builder, Matrix4f matrix, float alt, int overlay) {
        drawQuad(builder, matrix, 0.27f, -0.20f, 0.28f, 0.20f, overlay);
    }

    private void renderHeadingRibbon(VertexConsumer builder, Matrix4f matrix, float yaw, int overlay) {
        drawQuad(builder, matrix, -0.15f, 0.22f, 0.15f, 0.23f, overlay);
    }

    private void drawQuad(VertexConsumer builder, Matrix4f matrix, float minX, float minY, float maxX, float maxY, int overlay) {
        builder.addVertex(matrix, minX, minY, 0.0f).setColor(255, 160, 0, 220).setUv(0, 0).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
        builder.addVertex(matrix, maxX, minY, 0.0f).setColor(255, 160, 0, 220).setUv(1, 0).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
        builder.addVertex(matrix, maxX, maxY, 0.0f).setColor(255, 160, 0, 220).setUv(1, 1).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
        builder.addVertex(matrix, minX, maxY, 0.0f).setColor(255, 160, 0, 220).setUv(0, 1).setOverlay(overlay).setLight(0xF000F0).setNormal(0, 0, 1);
    }
}