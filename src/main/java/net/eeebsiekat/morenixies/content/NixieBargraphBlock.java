package net.eeebsiekat.morenixies.content;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import javax.annotation.Nullable;

public class NixieBargraphBlock extends DirectionalBlock implements EntityBlock, SimpleWaterloggedBlock, IWrenchable {
    public static final EnumProperty<BargraphPart> PART = EnumProperty.create("part", BargraphPart.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public NixieBargraphBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(PART, BargraphPart.SINGLE)
                .setValue(WATERLOGGED, false));
    }

    public static final com.mojang.serialization.MapCodec<NixieBargraphBlock> CODEC = simpleCodec(NixieBargraphBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide) return null;

        return blockEntityType == ModBlockEntities.NIXIE_BARGRAPH.get()
                ? (lvl, pos, st, be) -> NixieBargraphEntity.tick(lvl, pos, st, (NixieBargraphEntity) be)
                : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        return updatePartState(level, pos, facing, level.getFluidState(pos).getType() == net.minecraft.world.level.material.Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing.getAxis() != Direction.Axis.Y) {
            return state;
        }

        boolean hasAbove = level.getBlockState(currentPos.above()).getBlock() instanceof NixieBargraphBlock;
        boolean hasBelow = level.getBlockState(currentPos.below()).getBlock() instanceof NixieBargraphBlock;

        BargraphPart part;
        if (hasAbove && hasBelow) {
            part = BargraphPart.MIDDLE;
        } else if (hasAbove) {
            part = BargraphPart.START; // Bottom controller block
        } else if (hasBelow) {
            part = BargraphPart.END;   // Top cap block
        } else {
            part = BargraphPart.SINGLE;
        }

        return state.setValue(NixieBargraphBlock.PART, part);
    }

    private BlockState updatePartState(LevelAccessor level, BlockPos pos, Direction facing, boolean waterlogged) {
        boolean connectBack = isMatchingBargraph(level, pos.relative(facing.getOpposite()), facing);
        boolean connectForward = isMatchingBargraph(level, pos.relative(facing), facing);

        BargraphPart part;
        if (connectBack && connectForward) {
            part = BargraphPart.MIDDLE;
        } else if (connectBack) {
            part = BargraphPart.END;
        } else if (connectForward) {
            part = BargraphPart.START;
        } else {
            part = BargraphPart.SINGLE;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, part)
                .setValue(WATERLOGGED, waterlogged);
    }

    private boolean isMatchingBargraph(LevelAccessor level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof NixieBargraphBlock && state.getValue(FACING) == facing;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.NIXIE_BARGRAPH.get().create(pos, state);
    }
}