package net.eeebsiekat.morenixies.content;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.math.VoxelShaper;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NixieFlightHudBlock extends HorizontalDirectionalBlock implements EntityBlock, IWrenchable {

    public static final EnumProperty<HudPart> HUD_PART = EnumProperty.create("part", HudPart.class);

    public NixieFlightHudBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HUD_PART, HudPart.SINGLE));
    }

    private static final Map<HudPart, VoxelShaper> SHAPERS = createShapers();

    private static Map<HudPart, VoxelShaper> createShapers() {
        Map<HudPart, VoxelShaper> map = new EnumMap<>(HudPart.class);

        VoxelShape flatPanel = Block.box(0, 0, 0, 16, 19, 2);

        VoxelShape angledLeft = Shapes.or(
                Block.box(0, 0, 6, 4, 19, 8),
                Block.box(4, 0, 4, 8, 19, 6),
                Block.box(8, 0, 2, 12, 19, 4),
                Block.box(12, 0, 0, 16, 19, 2)
        );

        VoxelShape angledHardLeft = Shapes.or(
                Block.box(0, 0, 17, 3, 19, 20),
                Block.box(3, 0, 14, 6, 19, 17),
                Block.box(6, 0, 11, 9, 19, 14),
                Block.box(9, 0, 8, 12, 19, 11)
        );

        VoxelShape angledRight = Shapes.or(
                Block.box(0, 0, 0, 4, 19, 2),
                Block.box(4, 0, 2, 8, 19, 4),
                Block.box(8, 0, 4, 12, 19, 6),
                Block.box(12, 0, 6, 16, 19, 8)
        );

        VoxelShape angledHardRight = Shapes.or(
                Block.box(0, 0, 8, 3, 19, 11),
                Block.box(3, 0, 11, 6, 19, 14),
                Block.box(6, 0, 14, 9, 19, 17),
                Block.box(9, 0, 17, 12, 19, 20)
        );

        VoxelShaper flatShaper = VoxelShaper.forHorizontal(flatPanel, Direction.NORTH);
        VoxelShaper leftShaper = VoxelShaper.forHorizontal(angledLeft, Direction.NORTH);
        VoxelShaper leftHardShaper = VoxelShaper.forHorizontal(angledHardLeft, Direction.NORTH);
        VoxelShaper rightShaper = VoxelShaper.forHorizontal(angledRight, Direction.NORTH);
        VoxelShaper rightHardShaper = VoxelShaper.forHorizontal(angledHardRight, Direction.NORTH);

        map.put(HudPart.SINGLE, flatShaper);
        map.put(HudPart.MIDDLE, flatShaper);
        map.put(HudPart.SMALL_SIDE_LEFT_END, leftShaper);
        map.put(HudPart.SMALL_SIDE_LEFT, leftShaper);
        map.put(HudPart.LARGE_SIDE_LEFT, leftHardShaper);
        map.put(HudPart.SMALL_SIDE_RIGHT_END, rightShaper);
        map.put(HudPart.SMALL_SIDE_RIGHT, rightShaper);
        map.put(HudPart.LARGE_SIDE_RIGHT, rightHardShaper);

        return map;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        HudPart part = state.getValue(HUD_PART);
        Direction facing = state.getValue(FACING);

        VoxelShaper shaper = SHAPERS.get(part);
        return shaper != null ? shaper.get(facing) : super.getShape(state, level, pos, context);
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