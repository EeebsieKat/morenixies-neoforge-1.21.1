package net.eeebsiekat.morenixies.compat.aeronautics;

import dev.simulated_team.simulated.util.SimMovementContext;
import net.eeebsiekat.morenixies.content.NixieFlightHudEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;

public class AeronauticsBridge {
    public static boolean tryFetchContractionTelemetry(NixieFlightHudEntity hudEntity) {
        if (hudEntity.getLevel() == null) return false;

        // 1. Fetch the movement context using the block's center position
        SimMovementContext context = SimMovementContext.getMovementContext(hudEntity.getLevel(), Vec3.atCenterOf(hudEntity.getBlockPos()));
        if (context == null || context.subLevel() == null) return false;

        // 2. Extract orientation and global position directly from the context record
        Quaterniond orientation = context.orientation();
        Vec3 globalPos = context.globalPosition();

        // 3. Convert Quaternion to Euler Angles (Pitch, Yaw, Roll)
        double[] eulerAngles = quaternionToEulerDegrees(orientation);
        float pitch = (float) eulerAngles[0];
        float yaw = (float) eulerAngles[1];
        float roll = (float) eulerAngles[2];

        // 4. Supply telemetry back to the entity (matches the 5 parameters in updateFromAeronautics)
        hudEntity.updateFromAeronautics(pitch, roll, yaw, (float) globalPos.y(), 0.0f);
        return true;
    }

    private static double[] quaternionToEulerDegrees(Quaterniond q) {
        double sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
        double cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
        double roll = Math.atan2(sinr_cosp, cosr_cosp);

        double sinp = 2 * (q.w * q.y - q.z * q.x);
        double pitch;
        if (Math.abs(sinp) >= 1)
            pitch = Math.copySign(Math.PI / 2, sinp);
        else
            pitch = Math.asin(sinp);

        double siny_cosp = 2 * (q.w * q.z + q.x * q.y);
        double cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
        double yaw = Math.atan2(siny_cosp, cosy_cosp);

        return new double[]{Math.toDegrees(pitch), Math.toDegrees(yaw), Math.toDegrees(roll)};
    }
}