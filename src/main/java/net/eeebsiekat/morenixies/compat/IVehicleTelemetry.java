package net.eeebsiekat.morenixies.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public interface IVehicleTelemetry {
    /**
     * @return True if the block at this position is on a moving vehicle.
     */
    boolean isMounted(Level level, BlockPos pos);

    /**
     * @return The global velocity of the vehicle in m/s.
     */
    Vector3d getVelocity(Level level, BlockPos pos);

    /**
     * @return The global rotation of the vehicle.
     */
    Quaterniond getRotation(Level level, BlockPos pos);

    /**
     * @return The exact world-space Y altitude of the vehicle.
     */
    double getAltitude(Level level, BlockPos pos);
}