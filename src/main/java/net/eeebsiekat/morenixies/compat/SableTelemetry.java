package net.eeebsiekat.morenixies.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class SableTelemetry implements IVehicleTelemetry {

    @Override
    public boolean isMounted(Level level, BlockPos pos) {
        if (level == null) return false;
        return SableCompanion.INSTANCE.getContaining(level, pos) != null;
    }

    @Override
    public Vector3d getVelocity(Level level, BlockPos pos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel == null) return new Vector3d();

        // Pass the block's center as a Vec3 (which implements Position)
        Vec3 blockCenterPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3 vec = SableCompanion.INSTANCE.getVelocity(level, subLevel, blockCenterPos);

        if (vec == null) return new Vector3d();

        return new Vector3d(vec.x, vec.y, vec.z);
    }

    @Override
    public Quaterniond getRotation(Level level, BlockPos pos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel == null) return new Quaterniond();

        Pose3dc pose = subLevel.logicalPose();
        if (pose != null && pose.orientation() != null) {
            var q = pose.orientation();
            return new Quaterniond(q.x(), q.y(), q.z(), q.w());
        }
        return new Quaterniond();
    }

    @Override
    public double getAltitude(Level level, BlockPos pos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel == null) return pos.getY();

        Vector3d jomlPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vector3d globalPos = new Vector3d();

        Pose3dc pose = subLevel.logicalPose();
        if (pose != null) {
            transformPoint(pose, jomlPos, globalPos);
            return globalPos.y;
        }
        return pos.getY();
    }

    private void transformPoint(Pose3dc pose, Vector3d local, Vector3d dest) {
        dest.set(local);
        if (pose.scale() != null) {
            dest.mul(pose.scale().x(), pose.scale().y(), pose.scale().z());
        }
        if (pose.orientation() != null) {
            var q = pose.orientation();
            dest.rotate(new Quaterniond(q.x(), q.y(), q.z(), q.w()));
        }
        if (pose.position() != null) {
            dest.add(pose.position().x(), pose.position().y(), pose.position().z());
        }
    }
}