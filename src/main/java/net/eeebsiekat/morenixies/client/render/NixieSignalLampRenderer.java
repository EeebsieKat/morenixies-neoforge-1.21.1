package net.eeebsiekat.morenixies.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.content.NixieSignalLampBlock;
import net.eeebsiekat.morenixies.content.NixieSignalLampEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class NixieSignalLampRenderer implements BlockEntityRenderer<NixieSignalLampEntity> {

    public static final ResourceLocation INDICATOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(MoreNixies.MOD_ID, "block/nixie_indicator");

    private final BlockEntityRendererProvider.Context context;

    public NixieSignalLampRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(NixieSignalLampEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockState state = blockEntity.getBlockState();

        if (state.hasProperty(NixieSignalLampBlock.LIT) && !state.getValue(NixieSignalLampBlock.LIT)) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(INDICATOR_TEXTURE);

        float time = level.getGameTime() + partialTick;
        float flicker = 0.85f + 0.15f * (float) Math.sin(time * 0.75f) * (float) Math.cos(time * 0.31f);

        poseStack.pushPose();

        // 1. Move to block center
        poseStack.translate(0.5D, 0.5D, 0.5D);

        // 2. Rotate ONLY around Y-axis to face player (No pitch angle added)
        float cameraYRot = this.context.getBlockEntityRenderDispatcher().camera.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYRot));

        Matrix4f matrix = poseStack.last().pose();
        int light = 15728880;
        VertexConsumer builder = buffer.getBuffer(RenderType.cutout());

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // Preferred positioning bounds
        float minX = -0.14f;
        float maxX = 0.36f;
        float minY = -0.3f;
        float maxY = 0.2f;

        // Front Face (Facing Player)
        addVertex(builder, matrix, minX, minY, 0.0f, minU, maxV, light, flicker);
        addVertex(builder, matrix, maxX, minY, 0.0f, maxU, maxV, light, flicker);
        addVertex(builder, matrix, maxX, maxY, 0.0f, maxU, minV, light, flicker);
        addVertex(builder, matrix, minX, maxY, 0.0f, minU, minV, light, flicker);

        // Back Face (Mirrored order at exact same Z to prevent clipping)
        addVertex(builder, matrix, maxX, minY, 0.0f, maxU, maxV, light, flicker);
        addVertex(builder, matrix, minX, minY, 0.0f, minU, maxV, light, flicker);
        addVertex(builder, matrix, minX, maxY, 0.0f, minU, minV, light, flicker);
        addVertex(builder, matrix, maxX, maxY, 0.0f, maxU, minV, light, flicker);

        poseStack.popPose();
    }

    private void addVertex(VertexConsumer builder, Matrix4f matrix, float x, float y, float z, float u, float v, int light, float alpha) {
        builder.addVertex(matrix, x, y, z)
                .setColor(1.0f, 1.0f, 1.0f, alpha)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, 0, 1);
    }
}