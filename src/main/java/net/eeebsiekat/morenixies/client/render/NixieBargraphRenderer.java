package net.eeebsiekat.morenixies.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eeebsiekat.morenixies.MoreNixies;
import net.eeebsiekat.morenixies.content.BargraphPart;
import net.eeebsiekat.morenixies.content.NixieBargraphBlock;
import net.eeebsiekat.morenixies.content.NixieBargraphEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class NixieBargraphRenderer implements BlockEntityRenderer<NixieBargraphEntity> {

    public static final ResourceLocation BARGRAPH_TEXTURE = ResourceLocation.fromNamespaceAndPath(MoreNixies.MOD_ID, "block/nixie_graph");

    private final BlockEntityRendererProvider.Context context;

    public NixieBargraphRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(NixieBargraphEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof NixieBargraphBlock)) return;

        BargraphPart part = state.getValue(NixieBargraphBlock.PART);

        // Only render from the master base block (START or SINGLE)
        if (part != BargraphPart.START && part != BargraphPart.SINGLE) {
            return;
        }

        // Interpolate bounce animation between client ticks
        float rawLevel = blockEntity.getCurrentLevel();
        float velocity = blockEntity.getVelocity();
        float interpolatedLevel = rawLevel + (velocity * partialTick);

        // Clamp render bounds so the bounce doesn't clip past the glass ends
        float renderFill = Mth.clamp(interpolatedLevel, 0.0f, 1.0f);
        if (renderFill <= 0.0f) return;

        int chainLength = calculateVerticalChainLength(level, blockEntity.getBlockPos());
        float renderHeight = (chainLength * 1.0f - 0.25f) * renderFill;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(BARGRAPH_TEXTURE);

        float time = level.getGameTime() + partialTick;
        float alpha;

        if (blockEntity.isRedlined()) {
            // Fast strobe warning pulse for redline
            alpha = (Math.sin(time * 24.0f) > 0) ? 1.0f : 0.15f;
        } else {
            // Authentic CRT / Nixie Tube Micro-Flicker
            float baseHum = (float) Math.sin(time * 45.0f) * 0.05f;
            float harmonic = (float) Math.cos(time * 110.0f) * 0.03f;

            long posSeed = blockEntity.getBlockPos().asLong();
            float noise = (Mth.sin((float) (posSeed + level.getGameTime() * 17)) * 43758.5453f) % 1.0f;
            float microDrop = (noise > 0.92f) ? -0.25f : 0.0f;

            alpha = Mth.clamp(0.90f + baseHum + harmonic + microDrop, 0.40f, 1.0f);
        }

        poseStack.pushPose();

        // Center in block space and offset slightly above bottom rim
        poseStack.translate(0.5D, 0.125D, 0.5D);

        // Rotate around vertical Y axis to face camera
        float cameraYRot = this.context.getBlockEntityRenderDispatcher().camera.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYRot));

        Matrix4f matrix = poseStack.last().pose();
        int light = 15728880; // Full bright glow
        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        float minX = -0.125f;
        float maxX = 0.125f;

        float minY = 0.0f;
        float maxY = renderHeight;

        // Tile texture vertically along total height
        float vSpan = (maxV - minV) * (renderHeight * 4.0f);
        float currentMaxV = minV + (vSpan % (maxV - minV));

        // Front Quad
        addVertex(builder, matrix, minX, minY, 0.0f, minU, maxV, light, alpha);
        addVertex(builder, matrix, maxX, minY, 0.0f, maxU, maxV, light, alpha);
        addVertex(builder, matrix, maxX, maxY, 0.0f, maxU, currentMaxV, light, alpha);
        addVertex(builder, matrix, minX, maxY, 0.0f, minU, currentMaxV, light, alpha);

        // Back Quad
        addVertex(builder, matrix, maxX, minY, 0.0f, maxU, maxV, light, alpha);
        addVertex(builder, matrix, minX, minY, 0.0f, minU, maxV, light, alpha);
        addVertex(builder, matrix, minX, maxY, 0.0f, minU, currentMaxV, light, alpha);
        addVertex(builder, matrix, maxX, maxY, 0.0f, maxU, currentMaxV, light, alpha);

        poseStack.popPose();
    }

    private int calculateVerticalChainLength(Level level, BlockPos startPos) {
        int length = 1;
        BlockPos currentPos = startPos.above();

        for (int i = 1; i < 16; i++) {
            BlockState checkState = level.getBlockState(currentPos);
            if (checkState.getBlock() instanceof NixieBargraphBlock) {
                length++;
                if (checkState.getValue(NixieBargraphBlock.PART) == BargraphPart.END) {
                    break;
                }
                currentPos = currentPos.above();
            } else {
                break;
            }
        }
        return length;
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