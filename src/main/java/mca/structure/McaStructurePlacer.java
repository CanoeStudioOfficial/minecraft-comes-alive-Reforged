package mca.structure;

import mca.core.MCA;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/** Server-side, batched building placement. This is deliberately independent of blueprints. */
public final class McaStructurePlacer {
    private static final int BLOCKS_PER_TICK = 1200;
    private static final int MAX_QUEUE_SIZE = 2;
    private static final Deque<PendingPlacement> QUEUE = new ArrayDeque<>();
    private static PendingPlacement current;

    private McaStructurePlacer() {
    }

    public static boolean enqueue(World world, McaStructure structure, BlockPos anchor, Consumer<Boolean> result) {
        if (world == null || world.isRemote || structure == null || QUEUE.size() + (current == null ? 0 : 1) >= MAX_QUEUE_SIZE || !isAreaLoaded(world, structure, anchor)) {
            return false;
        }

        QUEUE.addLast(new PendingPlacement(world, structure, anchor, result));
        return true;
    }

    public static void tick() {
        int remaining = BLOCKS_PER_TICK;
        while (remaining > 0) {
            if (current == null) {
                current = QUEUE.pollFirst();
                if (current == null) {
                    return;
                }
            }

            if (current.index >= current.blocks.size()) {
                finish(true);
                continue;
            }

            if (!current.cleared) {
                int volume = current.structure.getWidth() * current.structure.getHeight() * current.structure.getLength();
                int index = current.clearIndex++;
                int x = index % current.structure.getWidth();
                int z = (index / current.structure.getWidth()) % current.structure.getLength();
                int y = index / (current.structure.getWidth() * current.structure.getLength());
                current.world.setBlockToAir(getTargetPosition(current.structure, current.anchor, x, y, z));
                if (current.clearIndex >= volume) {
                    current.cleared = true;
                }
                remaining--;
                continue;
            }

            McaStructure.ResolvedBlock block = current.blocks.get(current.index++);
            BlockPos target = getTargetPosition(current.structure, current.anchor, block);
            IBlockState state = block.getState();
            current.world.setBlockState(target, state, 2);
            remaining--;
        }
    }

    private static void finish(boolean success) {
        PendingPlacement completed = current;
        current = null;
        if (completed.result != null) {
            try {
                completed.result.accept(success);
            } catch (RuntimeException e) {
                MCA.getLog().error("MCA structure completion callback failed", e);
            }
        }
    }

    private static boolean isAreaLoaded(World world, McaStructure structure, BlockPos anchor) {
        int minX = anchor.getX() - structure.getWidth() / 2;
        int minZ = anchor.getZ() - structure.getLength() / 2;
        BlockPos min = new BlockPos(minX, anchor.getY() - 1, minZ);
        BlockPos max = new BlockPos(minX + structure.getWidth() - 1, anchor.getY() + structure.getHeight() - 2, minZ + structure.getLength() - 1);
        return world.isAreaLoaded(min, max, false);
    }

    private static BlockPos getTargetPosition(McaStructure structure, BlockPos anchor, McaStructure.ResolvedBlock block) {
        return getTargetPosition(structure, anchor, block.getX(), block.getY(), block.getZ());
    }

    private static BlockPos getTargetPosition(McaStructure structure, BlockPos anchor, int x, int y, int z) {
        int originX = anchor.getX() - structure.getWidth() / 2;
        int originY = anchor.getY() - 1;
        int originZ = anchor.getZ() - structure.getLength() / 2;
        return new BlockPos(originX + x, originY + y, originZ + z);
    }

    private static final class PendingPlacement {
        private final World world;
        private final McaStructure structure;
        private final BlockPos anchor;
        private final Consumer<Boolean> result;
        private final java.util.List<McaStructure.ResolvedBlock> blocks;
        private int index;
        private int clearIndex;
        private boolean cleared;

        private PendingPlacement(World world, McaStructure structure, BlockPos anchor, Consumer<Boolean> result) {
            this.world = world;
            this.structure = structure;
            this.anchor = anchor;
            this.result = result;
            this.blocks = new java.util.ArrayList<>(structure.getResolvedBlocks());
        }
    }
}
