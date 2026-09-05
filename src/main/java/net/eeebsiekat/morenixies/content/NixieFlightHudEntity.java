package net.eeebsiekat.morenixies.content;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.eeebsiekat.morenixies.compat.IVehicleTelemetry;
import net.eeebsiekat.morenixies.compat.SableTelemetry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.List;

public class NixieFlightHudEntity extends SmartBlockEntity {

    public enum DisplayMode implements StringRepresentable {
        OFF("off"),
        PITCH_ROLL("pitch_roll"),
        SPEED("speed"),
        ALTITUDE("altitude"),
        HEADING("heading"),
        TANK_FULLNESS("tank_fullness");

        private final String name;

        DisplayMode(String name) {
            this.name = name;
        }

        public DisplayMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    // Telemetry Data
    public float pitch, roll, yaw;
    public float speed;
    public float altitude;
    public float verticalVelocity;

    // Previous tick values for smooth interpolation in the renderer
    public float prevPitch, prevRoll, prevYaw;
    public float prevSpeed;
    public float prevAltitude;
    public float prevVerticalVelocity;

    private DisplayMode mode = DisplayMode.PITCH_ROLL;
    private IVehicleTelemetry telemetry;

    public NixieFlightHudEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NixieFlightHudEntity be) {
        // 1. Store previous values for interpolation
        be.prevPitch = be.pitch;
        be.prevRoll = be.roll;
        be.prevYaw = be.yaw;
        be.prevSpeed = be.speed;
        be.prevAltitude = be.altitude;
        be.prevVerticalVelocity = be.verticalVelocity;

        // 2. Try Aeronautics integration first
        if (net.neoforged.fml.ModList.get().isLoaded("aeronautics")) {
            if (net.eeebsiekat.morenixies.compat.aeronautics.AeronauticsBridge.tryFetchContractionTelemetry(be)) {
                return;
            }
        }

        // 3. Fallback to Sable integration
        if (be.telemetry == null && net.neoforged.fml.ModList.get().isLoaded("sable")) {
            be.telemetry = new SableTelemetry();
        }

        if (be.telemetry != null && be.telemetry.isMounted(level, pos)) {
            Vector3d vel = be.telemetry.getVelocity(level, pos);
            be.speed = (float) vel.length();
            be.verticalVelocity = (float) vel.y;
            be.altitude = (float) be.telemetry.getAltitude(level, pos);

            Quaterniond rot = be.telemetry.getRotation(level, pos);

            Vector3d forward = new Vector3d(0, 0, 1).rotate(rot);
            Vector3d up = new Vector3d(0, 1, 0).rotate(rot);

            be.pitch = (float) Math.toDegrees(Math.asin(Math.clamp(forward.y, -1.0, 1.0)));
            be.yaw = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));

            Vector3d right = new Vector3d(1, 0, 0).rotate(rot);
            be.roll = (float) Math.toDegrees(Math.atan2(right.y, up.y));
        }
    }

    public void cycleMode() {
        this.mode = this.mode.next();
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public DisplayMode getMode() {
        return mode;
    }

    public void setMode(DisplayMode mode) {
        this.mode = mode;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // Interpolation getters used by NixieFlightHudRenderer
    public float getInterpolatedPitch(float pt) {
        return prevPitch + (pitch - prevPitch) * pt;
    }

    public float getInterpolatedRoll(float pt) {
        return prevRoll + (roll - prevRoll) * pt;
    }

    public float getInterpolatedYaw(float pt) {
        return prevYaw + (yaw - prevYaw) * pt;
    }

    public float getInterpolatedSpeed(float pt) {
        return prevSpeed + (speed - prevSpeed) * pt;
    }

    public float getInterpolatedAltitude(float pt) {
        return prevAltitude + (altitude - prevAltitude) * pt;
    }

    public float getInterpolatedVerticalSpeed(float pt) {
        return prevVerticalVelocity + (verticalVelocity - prevVerticalVelocity) * pt;
    }

    public void updateFromAeronautics(float p, float r, float y, float alt, float spd, float vSpd) {
        this.pitch = p;
        this.roll = r;
        this.yaw = y;
        this.altitude = alt;
        this.speed = spd;
        this.verticalVelocity = vSpd;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("DisplayMode", mode.ordinal());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("DisplayMode")) {
            int modeOrdinal = tag.getInt("DisplayMode");
            DisplayMode[] modes = DisplayMode.values();
            if (modeOrdinal >= 0 && modeOrdinal < modes.length) {
                this.mode = modes[modeOrdinal];
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        write(tag, registries, true);
        return tag;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}