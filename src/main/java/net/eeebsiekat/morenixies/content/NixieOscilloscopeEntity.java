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

public class NixieOscilloscopeEntity extends BlockEntity {

    public enum DisplayMode {
        SPEED("Speed (m/s)");

        private final String displayName;
        DisplayMode(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public static final int SAMPLES = 100;
    private final float[] history = new float[SAMPLES];

    private float timeSpanSeconds = 5.0f;
    private DisplayMode mode = DisplayMode.SPEED;
    private long startTick = 0;
    private float currentProgress = 0f;

    // Multiblock configuration
    private int screenWidth = 1;
    private int screenHeight = 1;
    private int localX = 0;
    private int localY = 0;
    private boolean isController = false;

    private static final SableTelemetry TELEMETRY = new SableTelemetry();

    public NixieOscilloscopeEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIXIE_OSCILLOSCOPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NixieOscilloscopeEntity be) {
        if (level.isClientSide) return;

        if (level.getGameTime() % 20 == 0) {
            be.recalculateScreenBounds();
        }

        if (!be.isController) return;

        if (be.startTick == 0) {
            be.startTick = level.getGameTime();
        }

        long totalSpanTicks = (long) (be.timeSpanSeconds * 20f);
        long elapsed = (level.getGameTime() - be.startTick) % totalSpanTicks;
        be.currentProgress = (float) elapsed / (float) totalSpanTicks;

        int currentIndex = Math.min((int) (be.currentProgress * SAMPLES), SAMPLES - 1);

        float currentValue = 0f;
        if (be.mode == DisplayMode.SPEED) {
            if (TELEMETRY.isMounted(level, pos)) {
                Vector3d velocity = TELEMETRY.getVelocity(level, pos);
                currentValue = (float) velocity.length();
            } else {
                currentValue = level.getBestNeighborSignal(pos);
            }
        }

        be.history[currentIndex] = currentValue;

        be.setChanged();
        if (level.getGameTime() % 2 == 0) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public void cycleTimeSpan() {
        if (timeSpanSeconds == 2.0f) timeSpanSeconds = 5.0f;
        else if (timeSpanSeconds == 5.0f) timeSpanSeconds = 10.0f;
        else if (timeSpanSeconds == 10.0f) timeSpanSeconds = 30.0f;
        else timeSpanSeconds = 2.0f;

        this.startTick = level != null ? level.getGameTime() : 0;
        syncToClients();
    }

    public void cycleMode() {
        DisplayMode[] modes = DisplayMode.values();
        this.mode = modes[(this.mode.ordinal() + 1) % modes.length];
        syncToClients();
    }

    private void syncToClients() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public NixieOscilloscopeEntity getControllerEntity() {
        if (isController || level == null) return this;
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction right = facing.getClockWise();
        BlockPos controllerPos = worldPosition.relative(right.getOpposite(), localX).below(localY);

        if (level.getBlockEntity(controllerPos) instanceof NixieOscilloscopeEntity controllerBE) {
            return controllerBE;
        }
        return this;
    }

    public float getTimeSpanSeconds() { return getControllerEntity().timeSpanSeconds; }
    public float getCurrentProgress() { return getControllerEntity().currentProgress; }
    public DisplayMode getMode() { return getControllerEntity().mode; }

    public float[] getHistoryBuffer() {
        return getControllerEntity().history;
    }

    public void recalculateScreenBounds() {
        if (level == null || level.isClientSide) return;

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction right = facing.getClockWise();

        int minX = 0;
        while (isSameCasing(worldPosition.relative(right.getOpposite(), Math.abs(minX) + 1), facing)) minX--;

        int maxX = 0;
        while (isSameCasing(worldPosition.relative(right, maxX + 1), facing)) maxX++;

        int minY = 0;
        while (isSameCasing(worldPosition.below(Math.abs(minY) + 1), facing)) minY--;

        int maxY = 0;
        while (isSameCasing(worldPosition.above(maxY + 1), facing)) maxY++;

        this.screenWidth = (maxX - minX) + 1;
        this.screenHeight = (maxY - minY) + 1;
        this.localX = Math.abs(minX);
        this.localY = Math.abs(minY);
        this.isController = (this.localX == 0 && this.localY == 0);

        syncToClients();
    }

    private boolean isSameCasing(BlockPos pos, Direction facing) {
        BlockState st = level.getBlockState(pos);
        return st.is(getBlockState().getBlock()) && st.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }

    public boolean isController() { return isController; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < SAMPLES; i++) {
            tag.putFloat("hist_" + i, history[i]);
        }
        tag.putFloat("timeSpanSeconds", timeSpanSeconds);
        tag.putInt("modeOrdinal", mode.ordinal());
        tag.putLong("startTick", startTick);
        tag.putFloat("currentProgress", currentProgress);
        tag.putInt("screenWidth", screenWidth);
        tag.putInt("screenHeight", screenHeight);
        tag.putInt("localX", localX);
        tag.putInt("localY", localY);
        tag.putBoolean("isController", isController);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < SAMPLES; i++) {
            history[i] = tag.getFloat("hist_" + i);
        }
        timeSpanSeconds = tag.contains("timeSpanSeconds") ? tag.getFloat("timeSpanSeconds") : 5.0f;
        if (tag.contains("modeOrdinal")) {
            int ord = tag.getInt("modeOrdinal");
            DisplayMode[] modes = DisplayMode.values();
            mode = (ord >= 0 && ord < modes.length) ? modes[ord] : DisplayMode.SPEED;
        }
        startTick = tag.getLong("startTick");
        currentProgress = tag.getFloat("currentProgress");
        screenWidth = Math.max(1, tag.getInt("screenWidth"));
        screenHeight = Math.max(1, tag.getInt("screenHeight"));
        localX = tag.getInt("localX");
        localY = tag.getInt("localY");
        isController = tag.getBoolean("isController");
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