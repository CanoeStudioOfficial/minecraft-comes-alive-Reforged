package mca.util;

import com.google.gson.Gson;
import mca.api.objects.Pos;
import mca.core.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
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
     * Finds the highest non-air block position at the given x,z coordinates starting from the specified y position.
     * Returns the y position of the first air block above the found ground block.
     *
     * @param world The world to search in
     * @param x     The x coordinate to check
     * @param y     The starting y coordinate to begin searching downward from
     * @param z     The z coordinate to check
     * @return The y coordinate of the first air block above ground level
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
     * Converts an Entity's position to a Pos wrapper object.
     *
     * @param entity The entity to get the position from
     * @return A Pos object representing the entity's position
     */
    public static Pos wrapPos(Entity entity) {
        return new Pos(entity.getPosition());
    }

    /**
     * Reads a resource file from the JAR and returns its contents as a string.
     *
     * @param path The path to the resource relative to the assets/mca/ directory
     * @return The contents of the resource file as a string
     * @throws RuntimeException if the resource cannot be read
     */
    public static String readResource(String path) {
        String location = RESOURCE_PREFIX + path;

        try (InputStreamReader reader = new InputStreamReader(
                MCA.class.getClassLoader().getResourceAsStream(location))) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource from JAR: " + location, e);
        }
    }

    /**
     * Reads a JSON resource file from the JAR and parses it into the specified type.
     *
     * @param path The path to the JSON resource relative to the assets/mca/ directory
     * @param type The class type to deserialize the JSON into
     * @param <T>  The type of object to return
     * @return The deserialized object from the JSON resource
     */
    public static <T> T readResourceAsJSON(String path, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(readResource(path), type);
    }

    /**
     * Finds an entity in the world by its UUID.
     *
     * @param world The world to search in
     * @param uuid  The UUID of the entity to find
     * @return An Optional containing the found entity, or empty if not found
     */
    public static Optional<Entity> getEntityByUUID(World world, UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID())) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds an entity of a specific type in the world by its UUID.
     *
     * @param world The world to search in
     * @param uuid  The UUID of the entity to find
     * @param clazz The class of the entity type to search for
     * @param <T>   The type of entity to return
     * @return An Optional containing the found entity of the specified type, or empty if not found
     */
    public static <T extends Entity> Optional<T> getEntityByUUID(World world, UUID uuid, Class<? extends T> clazz) {
        for (Entity entity : world.loadedEntityList) {
            if (clazz.isAssignableFrom(entity.getClass()) && uuid.equals(entity.getUniqueID())) {
                return Optional.of((T) entity);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets a list of block positions near the origin that match the optional filter.
     *
     * @param origin The center position to search around
     * @param world  The world to search in
     * @param filter Optional block class filter (null for all blocks)
     * @param xzDist The horizontal search radius
     * @param yDist  The vertical search radius
     * @return A list of block positions matching the criteria
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
     * Finds the nearest block position from a list to the given origin position.
     *
     * @param origin The reference position to measure distance from
     * @param blocks The list of block positions to search through
     * @return The nearest block position, or null if the list is empty
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