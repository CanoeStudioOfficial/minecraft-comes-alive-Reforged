package mca.util;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {
    private static final String RESOURCE_PREFIX = "assets/mca/";

    /**
     * Finds the first safe spawn position above the ground.
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

    /**
     * Reads a resource from the mod JAR as a string.
     */
    public static String readResource(String path) {
        String location = RESOURCE_PREFIX + path;
        InputStream stream = MCA.class.getResourceAsStream("/" + location);

        if (stream == null) {
            throw new RuntimeException("Failed to find resource: " + location);
        }

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + location, e);
        }
    }

    /**
     * Parses a resource as a JSON object.
     */
    public static <T> T readResourceAsJSON(String path, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(readResource(path), type);
    }

    /**
     * Finds an entity by UUID.
     */
    public static Optional<Entity> getEntityByUUID(World world, UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID())) {
                return Optional.of(entity);
            }
        }
        return Optional.absent();
    }

    /**
     * Finds an entity of the requested type by UUID.
     */
    public static <T extends Entity> Optional<T> getEntityByUUID(World world, UUID uuid, Class<? extends T> clazz) {
        for (Entity entity : world.loadedEntityList) {
            if (clazz.isAssignableFrom(entity.getClass()) && uuid.equals(entity.getUniqueID())) {
                return Optional.of((T) entity);
            }
        }
        return Optional.absent();
    }

    /**
     * Collects nearby block positions matching the requested type.
     */
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

    /**
     * Returns the block position nearest to the origin.
     */
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
