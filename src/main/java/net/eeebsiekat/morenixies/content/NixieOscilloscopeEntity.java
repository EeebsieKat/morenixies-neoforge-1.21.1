package net.eeebsiekat.morenixies.content;

import net.eeebsiekat.morenixies.compat.SableTelemetry;
import net.eeebsiekat.morenixies.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

public class NixieOscilloscopeEntity extends BlockEntity {

    public static final int BUFFER_SIZE = 128; // Expanded buffer for multi-block span
    private final float[] history = new float[BUFFER_SIZE];
    private int head = 0;

    // Multi-block structure attributes
    private BlockPos controllerPos;
    private int width = 1;
    private int height = 1;
    private int offsetX = 0; // Local column index inside screen
    private int offsetY = 0; // Local row index inside screen

    private static final SableTelemetry TELEMETRY = new SableTelemetry();

    public NixieOscilloscopeEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIXIE_OSCILLOSCOPE.get(), pos, state);
        this.controllerPos = pos;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NixieOscilloscopeEntity be) {
        if (level.isClientSide) return;

        // Recalculate screen connectivity periodically or when updated
        if (level.getGameTime() % 20 == 0) {
            be.updateMultiblockStructure();
        }

        // Only controller tracks and syncs signal metrics
        if (!be.isController()) return;

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        float currentValue = be.readInputSignal(level, pos, facing);
        be.pushValue(currentValue);
    }

    private float readInputSignal(Level level, BlockPos pos, Direction facing) {
        float signal = 0f;

        // 1. Redstone Signal (Checks back and sides)
        int redstone = level.getBestNeighborSignal(pos);
        if (redstone > 0) {
            signal = Math.max(signal, redstone);
        }

        // 2. Telemetry / Sable Speed Metric
        if (TELEMETRY.isMounted(level, pos)) {
            Vector3d velocity = TELEMETRY.getVelocity(level, pos);
            signal = Math.max(signal, (float) velocity.length() * 10f); // Scale for trace view
        }

        // 3. Create Kinetic RPM (Optional dynamic check via reflection or block state if Create is present)
        // If your setup uses Create's KineticBlockEntity, pull speed here:
        // if (be instanceof KineticBlockEntity kbe) signal = Math.abs(kbe.getSpeed());

        return signal;
    }

    public void updateMultiblockStructure() {
        if (level == null || level.isClientSide) return;

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction right = facing.getClockWise();

        // Scan contiguous grid bounds facing the same direction
        Set<BlockPos> connected = new HashSet<>();
        findConnectedGrid(worldPosition, facing, connected);

        int minX = 0, maxX = 0, minY = 0, maxY = 0;

        for (BlockPos p : connected) {
            int relX = (p.getX() - worldPosition.getX()) * right.getStepX() + (p.getZ() - worldPosition.getZ()) * right.getStepZ();
            int relY = p.getY() - worldPosition.getY();

            minX = Math.min(minX, relX);
            maxX = Math.max(maxX, relX);
            minY = Math.min(minY, relY);
            maxY = Math.max(maxY, relY);
        }

        // Top-Left block acts as Primary Controller
        BlockPos origin = worldPosition.relative(right, minX).above(maxY);
        this.controllerPos = origin;
        this.width = (maxX - minX) + 1;
        this.height = (maxY - minY) + 1;

        int myX = (worldPosition.getX() - origin.getX()) * right.getStepX() + (worldPosition.getZ() - origin.getZ()) * right.getStepZ();
        int myY = origin.getY() - worldPosition.getY();
        this.offsetX = myX;
        this.offsetY = myY;

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void findConnectedGrid(BlockPos current, Direction facing, Set<BlockPos> visited) {
        if (visited.contains(current)) return;

        BlockState st = level.getBlockState(current);
        if (!st.is(getBlockState().getBlock()) || st.getValue(BlockStateProperties.HORIZONTAL_FACING) != facing) {
            return;
        }

        visited.add(current);
        Direction right = facing.getClockWise();

        findConnectedGrid(current.relative(right), facing, visited);
        findConnectedGrid(current.relative(right.getOpposite()), facing, visited);
        findConnectedGrid(current.above(), facing, visited);
        findConnectedGrid(current.below(), facing, visited);
    }

    public boolean isController() {
        return worldPosition.equals(controllerPos);
    }

    private void pushValue(float value) {
        history[head] = value;
        head = (head + 1) % BUFFER_SIZE;
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public float[] getHistoryBuffer() {
        float[] ordered = new float[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            ordered[i] = history[(head + i) % BUFFER_SIZE];
        }
        return ordered;
    }

    public int getScreenWidth() { return width; }
    public int getScreenHeight() { return height; }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < BUFFER_SIZE; i++) {
            tag.putFloat("hist_" + i, history[i]);
        }
        tag.putInt("head", head);
        tag.putInt("width", width);
        tag.putInt("height", height);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < BUFFER_SIZE; i++) {
            history[i] = tag.getFloat("hist_" + i);
        }
        head = tag.getInt("head");
        width = Math.max(1, tag.getInt("width"));
        height = Math.max(1, tag.getInt("height"));
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}