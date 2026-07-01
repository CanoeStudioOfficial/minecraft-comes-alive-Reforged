package mca.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class MCACollisionUtil {
    private static final double MAX_PASSABLE_CARPET_HEIGHT = 0.125D;
    private static final double EPSILON = 1.0E-7D;

    private MCACollisionUtil() {
    }

    public static AxisAlignedBB getPassableCarpetCollisionBox(IBlockAccess world, BlockPos pos) {
        return getPassableCarpetCollisionBox(world, pos, world.getBlockState(pos));
    }

    public static AxisAlignedBB getPassableCarpetCollisionBox(IBlockAccess world, BlockPos pos, IBlockState state) {
        AxisAlignedBB localBox = state.getCollisionBoundingBox(world, pos);
        if (localBox == null || !isCarpetLikeBlock(state) || !isThinFloorLayer(localBox)) {
            return null;
        }

        return localBox.offset(pos);
    }

    public static boolean isPassableCarpet(IBlockAccess world, BlockPos pos) {
        return getPassableCarpetCollisionBox(world, pos) != null;
    }

    public static boolean isSameBox(AxisAlignedBB box, AxisAlignedBB other) {
        return nearlyEqual(box.minX, other.minX)
                && nearlyEqual(box.minY, other.minY)
                && nearlyEqual(box.minZ, other.minZ)
                && nearlyEqual(box.maxX, other.maxX)
                && nearlyEqual(box.maxY, other.maxY)
                && nearlyEqual(box.maxZ, other.maxZ);
    }

    private static boolean isCarpetLikeBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockCarpet || state.getMaterial() == Material.CARPET) {
            return true;
        }

        ResourceLocation id = block.getRegistryName();
        if (id == null) {
            return false;
        }

        String path = id.getPath().toLowerCase();
        return path.contains("carpet") || path.contains("rug");
    }

    private static boolean isThinFloorLayer(AxisAlignedBB box) {
        return nearlyEqual(box.minY, 0.0D)
                && box.maxY > 0.0D
                && box.maxY <= MAX_PASSABLE_CARPET_HEIGHT
                && box.maxX - box.minX <= 1.0D
                && box.maxZ - box.minZ <= 1.0D;
    }

    private static boolean nearlyEqual(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }
}
