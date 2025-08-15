package mca.util;

import com.google.gson.Gson;
import mca.api.objects.Pos;
import mca.api.wrappers.WorldWrapper;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Util {
    private static final String RESOURCE_PREFIX = "assets/mca/";

    /**
     * Finds a y position given an x,y,z coordinate triple that is assumed to be the world's "ground".
     *
     * @param world	The world in which blocks will be tested
     * @param x			X coordinate
     * @param y			Y coordinate, used as the starting height for finding ground.
     * @param z			Z coordinate
     * @return Integer representing the air block above the first non-air block given the provided ordered triples.
     */
    public static int getSpawnSafeTopLevel(World world, int x, int y, int z) {
        BlockPos pos;
        IBlockState state;

        while (y > 0) {
            pos = new BlockPos(x, y, z);
            state = world.getBlockState(pos);
            if (!state.getBlock().isAir(state, world, pos)) {
                break;
            }
            y--;

        }

        return y + 1;
    }

    public static Pos wrapPos(Entity entity) {
        return new Pos(entity.getPosition());
    }

    public static String readResource(String path) {
        String location = RESOURCE_PREFIX + path;

        try (InputStreamReader reader = new InputStreamReader(
                MCA.class.getClassLoader().getResourceAsStream(location))) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource from JAR: " + location, e);
        }


    }

    public static <T> T readResourceAsJSON(String path, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(readResource(path), type);

    }

    public static Optional<Entity> getEntityByUUID(World world, UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID())) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    public static <T extends Entity> Optional<T> getEntityByUUID(World world, UUID uuid, Class<? extends T> clazz) {
        for (Entity entity : world.loadedEntityList) {
            if (clazz.isAssignableFrom(entity.getClass()) && uuid.equals(entity.getUniqueID())) {
                return Optional.of((T) entity);
            }
        }
        return Optional.empty();
    }

    public static List<BlockPos> getNearbyBlocks(BlockPos origin, World world, @Nullable Class<? extends Block> filter, int xzDist, int yDist) {
        List<BlockPos> result = new ArrayList<>();
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();

        for (int x = -xzDist; x <= xzDist; x++) {
            for (int y = -yDist; y <= yDist; y++) {
                for (int z = -xzDist; z <= xzDist; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos pos = new BlockPos(ox + x, oy + y, oz + z);
                    Block block = world.getBlockState(pos).getBlock();

                    if (filter == null || filter.isAssignableFrom(block.getClass())) {
                        result.add(pos);
                    }
                }
            }
        }
        return result;
    }

    public static BlockPos getNearestPoint(BlockPos origin, List<BlockPos> blocks) {
        BlockPos nearest = null;
        double minDistSq = Double.MAX_VALUE;

        for (BlockPos target : blocks) {
            double distSq = origin.distanceSq(target);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = target;
            }
        }

        return nearest;
    }

}
