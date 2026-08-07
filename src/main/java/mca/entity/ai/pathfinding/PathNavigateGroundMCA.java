package mca.entity.ai.pathfinding;

import mca.util.MCACollisionUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateGroundMCA extends PathNavigateGround {
    public PathNavigateGroundMCA(EntityLiving entity, World world) {
        super(entity, world);
    }

    @Override
    protected PathFinder getPathFinder() {
        boolean canEnterDoors = this.nodeProcessor == null || this.nodeProcessor.getCanEnterDoors();
        this.nodeProcessor = new WalkNodeProcessorMCA();
        this.nodeProcessor.setCanEnterDoors(canEnterDoors);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    protected boolean isDirectPathBetweenPoints(Vec3d start, Vec3d end, int sizeX, int sizeY, int sizeZ) {
        int x = MathHelper.floor(start.x);
        int z = MathHelper.floor(start.z);
        double deltaX = end.x - start.x;
        double deltaZ = end.z - start.z;
        double distanceSq = deltaX * deltaX + deltaZ * deltaZ;

        if (distanceSq < 1.0E-8D) {
            return false;
        }

        double distance = 1.0D / Math.sqrt(distanceSq);
        deltaX *= distance;
        deltaZ *= distance;
        sizeX += 2;
        sizeZ += 2;

        if (!this.isSafeToStandAt(x, (int) start.y, z, sizeX, sizeY, sizeZ, start, deltaX, deltaZ)) {
            return false;
        }

        sizeX -= 2;
        sizeZ -= 2;
        double stepX = 1.0D / Math.abs(deltaX);
        double stepZ = 1.0D / Math.abs(deltaZ);
        double offsetX = (double) x - start.x;
        double offsetZ = (double) z - start.z;

        if (deltaX >= 0.0D) {
            offsetX++;
        }

        if (deltaZ >= 0.0D) {
            offsetZ++;
        }

        offsetX /= deltaX;
        offsetZ /= deltaZ;
        int signX = deltaX < 0.0D ? -1 : 1;
        int signZ = deltaZ < 0.0D ? -1 : 1;
        int targetX = MathHelper.floor(end.x);
        int targetZ = MathHelper.floor(end.z);
        int remainingX = targetX - x;
        int remainingZ = targetZ - z;

        while (remainingX * signX > 0 || remainingZ * signZ > 0) {
            if (offsetX < offsetZ) {
                offsetX += stepX;
                x += signX;
                remainingX = targetX - x;
            } else {
                offsetZ += stepZ;
                z += signZ;
                remainingZ = targetZ - z;
            }

            if (!this.isSafeToStandAt(x, (int) start.y, z, sizeX, sizeY, sizeZ, start, deltaX, deltaZ)) {
                return false;
            }
        }

        return true;
    }

    private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d start, double deltaX, double deltaZ) {
        int minX = x - sizeX / 2;
        int minZ = z - sizeZ / 2;

        if (!this.isPositionClear(minX, y, minZ, sizeX, sizeY, sizeZ, start, deltaX, deltaZ)) {
            return false;
        }

        for (int checkX = minX; checkX < minX + sizeX; ++checkX) {
            for (int checkZ = minZ; checkZ < minZ + sizeZ; ++checkZ) {
                double offsetX = (double) checkX + 0.5D - start.x;
                double offsetZ = (double) checkZ + 0.5D - start.z;

                if (offsetX * deltaX + offsetZ * deltaZ >= 0.0D) {
                    PathNodeType nodeBelow = this.nodeProcessor.getPathNodeType(this.world, checkX, y - 1, checkZ, this.entity, sizeX, sizeY, sizeZ, true, true);

                    if (nodeBelow == PathNodeType.WATER || nodeBelow == PathNodeType.LAVA || nodeBelow == PathNodeType.OPEN) {
                        return false;
                    }

                    PathNodeType node = this.nodeProcessor.getPathNodeType(this.world, checkX, y, checkZ, this.entity, sizeX, sizeY, sizeZ, true, true);
                    float priority = this.entity.getPathPriority(node);

                    if (priority < 0.0F || priority >= 8.0F) {
                        return false;
                    }

                    if (node == PathNodeType.DAMAGE_FIRE || node == PathNodeType.DANGER_FIRE || node == PathNodeType.DAMAGE_OTHER) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d start, double deltaX, double deltaZ) {
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
            double offsetX = (double) pos.getX() + 0.5D - start.x;
            double offsetZ = (double) pos.getZ() + 0.5D - start.z;

            if (offsetX * deltaX + offsetZ * deltaZ >= 0.0D) {
                Block block = this.world.getBlockState(pos).getBlock();

                if (!block.isPassable(this.world, pos) && !MCACollisionUtil.isPassableCarpet(this.world, pos)) {
                    return false;
                }
            }
        }

        return true;
    }
}
