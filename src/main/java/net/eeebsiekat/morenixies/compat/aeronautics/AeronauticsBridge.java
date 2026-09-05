package net.eeebsiekat.morenixies.compat.aeronautics;

import dev.simulated_team.simulated.util.SimMovementContext;
import net.eeebsiekat.morenixies.compat.SableTelemetry;
import net.eeebsiekat.morenixies.content.NixieFlightHudEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class AeronauticsBridge {

    private static final SableTelemetry TELEMETRY = new SableTelemetry();

    public static boolean tryFetchContractionTelemetry(NixieFlightHudEntity hudEntity) {
        if (hudEntity.getLevel() == null) return false;

        BlockPos pos = hudEntity.getBlockPos();

        // 1. Fetch movement context
        SimMovementContext context = SimMovementContext.getMovementContext(hudEntity.getLevel(), Vec3.atCenterOf(pos));
        if (context == null || context.subLevel() == null) return false;

        // 2. Extract orientation and position
        Quaterniond orientation = context.orientation();
        Vec3 globalPos = context.globalPosition();

        // 3. Convert Quaternion to Euler Angles
        double[] eulerAngles = quaternionToEulerDegrees(orientation);

        // 4. Exact rotation mapping
        float pitch = (float) eulerAngles[2];
        float roll  = (float) eulerAngles[1];
        float yaw   = (float) eulerAngles[0];

        // 5. Compute velocity & scalar speed (m/s)
        Vector3d velocity = TELEMETRY.getVelocity(hudEntity.getLevel(), pos);
        float speed = (float) velocity.length();
        float verticalSpeed = (float) velocity.y;

        // 6. Pass complete telemetry to entity
        hudEntity.updateFromAeronautics(pitch, roll, yaw, (float) globalPos.y(), speed, verticalSpeed);
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