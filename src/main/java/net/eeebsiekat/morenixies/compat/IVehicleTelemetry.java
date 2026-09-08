package net.eeebsiekat.morenixies.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public interface IVehicleTelemetry {
    boolean isMounted(Level level, BlockPos pos);

    Vector3d getVelocity(Level level, BlockPos pos);

    Quaterniond getRotation(Level level, BlockPos pos);

    double getAltitude(Level level, BlockPos pos);
}