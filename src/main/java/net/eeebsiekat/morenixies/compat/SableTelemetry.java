package net.eeebsiekat.morenixies.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class SableTelemetry implements IVehicleTelemetry {

    @Override
    public boolean isMounted(Level level, BlockPos pos) {
        return SableCompanion.INSTANCE.getContaining(level, pos) != null;
    }

    @Override
    public Vector3d getVelocity(Level level, BlockPos pos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel == null) return new Vector3d();

        Vector3d jomlPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vector3d currentPos = new Vector3d();
        Vector3d lastPos = new Vector3d();

        Pose3dc logical = subLevel.logicalPose();
        Pose3dc last = subLevel.lastPose();

        if (logical != null && last != null) {
            transformPoint(logical, jomlPos, currentPos);
            transformPoint(last, jomlPos, lastPos);
            return currentPos.sub(lastPos).mul(20.0F);
        }

        return new Vector3d();
    }

    @Override
    public Quaterniond getRotation(Level level, BlockPos pos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel == null) return new Quaterniond();

        Pose3dc pose = subLevel.logicalPose();
        if (pose != null && pose.orientation() != null) {
            return new Quaterniond(pose.orientation());
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
            dest.mul(pose.scale());
        }
        if (pose.orientation() != null) {
            dest.rotate(pose.orientation());
        }
        if (pose.position() != null) {
            dest.add(pose.position());
        }
    }
}