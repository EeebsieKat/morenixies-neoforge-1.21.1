package net.eeebsiekat.morenixies.content;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class NixieFlightHudBlock extends DirectionalBlock implements EntityBlock, IWrenchable {

    public NixieFlightHudBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public static final com.mojang.serialization.MapCodec<NixieFlightHudBlock> CODEC = simpleCodec(NixieFlightHudBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Places facing the player, or opposite to the face clicked depending on preference.
        // For a HUD, you usually want it facing AWAY from the player so they can read it.
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.NIXIE_FLIGHT_HUD.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // We only really need to tick this on the client side for rendering purposes,
        // unless you plan to emit redstone signals based on altitude/speed.
        if (!level.isClientSide) return null;

        return blockEntityType == ModBlockEntities.NIXIE_FLIGHT_HUD.get()
                ? (lvl, pos, st, be) -> NixieFlightHudEntity.tick(lvl, pos, st, (NixieFlightHudEntity) be)
                : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof NixieFlightHudEntity be) {
            // Right Click with Wrench: Toggle Display Modes (e.g., Speed in m/s vs knots)
            if (stack.isEmpty() || stack.getItem().getDescriptionId().contains("wrench")) {
                if (!level.isClientSide) {
                    be.toggleDisplayMode();
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}