package net.eeebsiekat.morenixies.content;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NixieFlightHudBlock extends HorizontalDirectionalBlock implements EntityBlock, IWrenchable {

    public static final EnumProperty<HudPart> HUD_PART = EnumProperty.create("part", HudPart.class);

    public NixieFlightHudBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HUD_PART, HudPart.SINGLE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HUD_PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            updateMultiblockGroup(level, pos, state.getValue(FACING));
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction blockFacing = state.getValue(FACING);
        Direction leftDir = blockFacing.getCounterClockWise();
        Direction rightDir = blockFacing.getClockWise();

        if ((facing == leftDir || facing == rightDir) && level instanceof Level realLevel && !realLevel.isClientSide) {
            realLevel.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, facing, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            Direction facing = state.getValue(FACING);
            super.onRemove(state, level, pos, newState, isMoving);

            Direction leftDir = facing.getCounterClockWise();
            Direction rightDir = facing.getClockWise();

            if (level.getBlockState(pos.relative(leftDir)).is(this)) {
                updateMultiblockGroup(level, pos.relative(leftDir), facing);
            }
            if (level.getBlockState(pos.relative(rightDir)).is(this)) {
                updateMultiblockGroup(level, pos.relative(rightDir), facing);
            }
        } else {
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private void updateMultiblockGroup(Level level, BlockPos startPos, Direction facing) {
        Direction leftDir = facing.getCounterClockWise();
        Direction rightDir = facing.getClockWise();

        BlockPos leftmost = startPos;
        for (int i = 0; i < 4; i++) {
            BlockPos next = leftmost.relative(leftDir);
            BlockState nextState = level.getBlockState(next);
            if (nextState.is(this) && nextState.getValue(FACING) == facing) {
                leftmost = next;
            } else {
                break;
            }
        }

        List<BlockPos> connected = new ArrayList<>();
        BlockPos current = leftmost;
        while (connected.size() < 5) {
            BlockState currentState = level.getBlockState(current);
            if (currentState.is(this) && currentState.getValue(FACING) == facing) {
                connected.add(current);
                current = current.relative(rightDir);
            } else {
                break;
            }
        }

        int totalWidth = connected.size();

        for (int index = 0; index < totalWidth; index++) {
            BlockPos p = connected.get(index);
            HudPart targetPart = calculatePart(totalWidth, index);
            BlockState currentState = level.getBlockState(p);

            if (currentState.getValue(HUD_PART) != targetPart) {
                level.setBlock(p, currentState.setValue(HUD_PART, targetPart), 3);
            }
        }
    }

    private HudPart calculatePart(int totalWidth, int indexFromLeft) {
        if (totalWidth == 1) return HudPart.SINGLE;

        return switch (totalWidth) {
            case 2 -> (indexFromLeft == 0) ? HudPart.SMALL_SIDE_LEFT_END : HudPart.SMALL_SIDE_RIGHT_END;
            case 3 -> switch (indexFromLeft) {
                case 0 -> HudPart.SMALL_SIDE_LEFT_END;
                case 1 -> HudPart.MIDDLE;
                default -> HudPart.SMALL_SIDE_RIGHT_END;
            };
            case 4 -> switch (indexFromLeft) {
                case 0 -> HudPart.LARGE_SIDE_LEFT;
                case 1 -> HudPart.SMALL_SIDE_LEFT;
                case 2 -> HudPart.SMALL_SIDE_RIGHT;
                default -> HudPart.LARGE_SIDE_RIGHT;
            };
            case 5 -> switch (indexFromLeft) {
                case 0 -> HudPart.LARGE_SIDE_LEFT;
                case 1 -> HudPart.SMALL_SIDE_LEFT;
                case 2 -> HudPart.MIDDLE;
                case 3 -> HudPart.SMALL_SIDE_RIGHT;
                default -> HudPart.LARGE_SIDE_RIGHT;
            };
            default -> HudPart.SINGLE;
        };
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof NixieFlightHudEntity hudBe) {
                hudBe.cycleMode();
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NixieFlightHudEntity(ModBlockEntities.NIXIE_FLIGHT_HUD.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (lvl, pos, st, be) -> {
            if (be instanceof NixieFlightHudEntity hudEntity) {
                NixieFlightHudEntity.tick(lvl, pos, st, hudEntity);
            }
        };
    }
}