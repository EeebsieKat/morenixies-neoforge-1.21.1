package net.eeebsiekat.morenixies.content;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.eeebsiekat.morenixies.compat.IVehicleTelemetry;
import net.eeebsiekat.morenixies.compat.SableTelemetry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.List;

public class NixieFlightHudEntity extends SmartBlockEntity {

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

    private int displayMode = 0;
    private IVehicleTelemetry telemetry;

    public NixieFlightHudEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NixieFlightHudEntity be) {
        // Store previous values for interpolation
        be.prevPitch = be.pitch;
        be.prevRoll = be.roll;
        be.prevYaw = be.yaw;
        be.prevSpeed = be.speed;
        be.prevAltitude = be.altitude;
        be.prevVerticalVelocity = be.verticalVelocity;

        if (be.telemetry == null && net.neoforged.fml.ModList.get().isLoaded("sable")) {
            be.telemetry = new SableTelemetry();
        }

        if (be.telemetry != null && be.telemetry.isMounted(level, pos)) {
            Vector3d vel = be.telemetry.getVelocity(level, pos);
            be.speed = (float) vel.length();
            be.verticalVelocity = (float) vel.y;
            be.altitude = (float) be.telemetry.getAltitude(level, pos);

            Quaterniond rot = be.telemetry.getRotation(level, pos);

            // Extract proper intrinsic Tait-Bryan aircraft angles (Yaw -> Pitch -> Roll)
            Vector3d forward = new Vector3d(0, 0, 1).rotate(rot);
            Vector3d up = new Vector3d(0, 1, 0).rotate(rot);

            be.pitch = (float) Math.toDegrees(Math.asin(Math.clamp(forward.y, -1.0, 1.0)));
            be.yaw = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));

            // Calculate Roll relative to the horizon plane
            Vector3d right = new Vector3d(1, 0, 0).rotate(rot);
            be.roll = (float) Math.toDegrees(Math.atan2(right.y, up.y));
        }
    }

    public void toggleDisplayMode() {
        this.displayMode = (this.displayMode + 1) % 2;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public int getDisplayMode() {
        return displayMode;
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

    public boolean isShowPitchLadder() {
        return true;
    }

    public boolean isShowTapes() {
        return true;
    }

    public void updateFromAeronautics(float p, float r, float y, float alt, float vSpd) {
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
        this.prevYaw = this.yaw;
        this.prevAltitude = this.altitude;
        this.prevVerticalVelocity = this.verticalVelocity;

        this.pitch = p;
        this.roll = r;
        this.yaw = y;
        this.altitude = alt;
        this.verticalVelocity = vSpd;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}
}