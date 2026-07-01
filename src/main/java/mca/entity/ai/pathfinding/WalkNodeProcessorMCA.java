package mca.entity.ai.pathfinding;

import mca.util.MCACollisionUtil;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WalkNodeProcessorMCA extends WalkNodeProcessor {
    @Override
    protected PathNodeType getPathNodeTypeRaw(IBlockAccess blockAccess, int x, int y, int z) {
        if (MCACollisionUtil.isPassableCarpet(blockAccess, new BlockPos(x, y, z))) {
            return PathNodeType.OPEN;
        }

        return super.getPathNodeTypeRaw(blockAccess, x, y, z);
    }

    @Override
    public PathNodeType getPathNodeType(IBlockAccess blockAccess, int x, int y, int z) {
        PathNodeType type = super.getPathNodeType(blockAccess, x, y, z);
        if (type == PathNodeType.OPEN && y >= 1 && MCACollisionUtil.isPassableCarpet(blockAccess, new BlockPos(x, y - 1, z))) {
            return PathNodeType.WALKABLE;
        }

        return type;
    }
}
