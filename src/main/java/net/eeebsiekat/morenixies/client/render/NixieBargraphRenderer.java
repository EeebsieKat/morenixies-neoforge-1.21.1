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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class NixieBargraphRenderer implements BlockEntityRenderer<NixieBargraphEntity> {

    // Updated to use your nixie_graph texture
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

        // Render exclusively from the bottom master block
        if (part != BargraphPart.START && part != BargraphPart.SINGLE) {
            return;
        }

        float fillLevel = blockEntity.getCurrentLevel(); // 0.0f to 1.0f
        if (fillLevel <= 0.0f) return;

        int chainLength = calculateVerticalChainLength(level, blockEntity.getBlockPos());
        float totalLength = chainLength * 1.0f;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(BARGRAPH_TEXTURE);

        float time = level.getGameTime() + partialTick;

        // Flicker / Redline visual calculations
        float alpha;
        if (blockEntity.isRedlined()) {
            alpha = (Math.sin(time * 12.0f) > 0) ? 1.0f : 0.2f;
        } else {
            alpha = 0.85f + 0.15f * (float) Math.sin(time * 0.75f) * (float) Math.cos(time * 0.31f);
        }

        poseStack.pushPose();

        // Center horizontally (0.5, 0.5), anchor bottom near glass base (0.125)
        poseStack.translate(0.5D, 0.125D, 0.5D);

        // Face camera rotation
        float cameraYRot = this.context.getBlockEntityRenderDispatcher().camera.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYRot));

        Matrix4f matrix = poseStack.last().pose();
        int light = 15728880; // Full brightness neon glow
        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // 4px width scaling centered at origin
        float minX = -0.125f; // 4 pixels wide in world units
        float maxX = 0.125f;

        float minY = 0.0f;
        float heightSpan = (totalLength - 0.25f) * fillLevel;
        float maxY = minY + heightSpan;

        // Tile V coordinates so the 4x4 texture repeats instead of stretching endlessly
        float vSpan = (maxV - minV) * (heightSpan * 4.0f); // 4 tiles per block height
        float currentMaxV = minV + (vSpan % (maxV - minV));

        // Front Face
        addVertex(builder, matrix, minX, minY, 0.0f, minU, maxV, light, alpha);
        addVertex(builder, matrix, maxX, minY, 0.0f, maxU, maxV, light, alpha);
        addVertex(builder, matrix, maxX, maxY, 0.0f, maxU, currentMaxV, light, alpha);
        addVertex(builder, matrix, minX, maxY, 0.0f, minU, currentMaxV, light, alpha);

        // Back Face
        addVertex(builder, matrix, maxX, minY, 0.0f, maxU, maxV, light, alpha);
        addVertex(builder, matrix, minX, minY, 0.0f, minU, maxV, light, alpha);
        addVertex(builder, matrix, minX, maxY, 0.0f, minU, currentMaxV, light, alpha);
        addVertex(builder, matrix, maxX, maxY, 0.0f, maxU, currentMaxV, light, alpha);

        poseStack.popPose();
    }

    private int calculateVerticalChainLength(Level level, net.minecraft.core.BlockPos startPos) {
        int length = 1;
        net.minecraft.core.BlockPos currentPos = startPos.above();

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