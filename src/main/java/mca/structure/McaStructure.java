package mca.structure;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A compact, runtime-ready building definition exported from an old MCEdit schematic. */
public final class McaStructure {
    private String id;
    private int width;
    private int height;
    private int length;
    private String anchor;
    private List<String> palette = new ArrayList<>();
    private List<List<Integer>> blocks = new ArrayList<>();

    private transient List<ResolvedBlock> resolvedBlocks;

    public String getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int getBlockCount() {
        return blocks == null ? 0 : blocks.size();
    }

    public List<String> getPalette() {
        return palette == null ? Collections.emptyList() : Collections.unmodifiableList(palette);
    }

    public List<ResolvedBlock> getResolvedBlocks() {
        if (resolvedBlocks == null) {
            resolvedBlocks = new ArrayList<>();
            if (blocks == null || palette == null) {
                return resolvedBlocks;
            }

            for (List<Integer> entry : blocks) {
                if (entry == null || entry.size() < 5) {
                    continue;
                }

                int paletteIndex = entry.get(3);
                if (paletteIndex < 0 || paletteIndex >= palette.size()) {
                    throw new IllegalStateException("Invalid palette index in structure " + id + ": " + paletteIndex);
                }

                Block block = Block.getBlockFromName(palette.get(paletteIndex));
                if (block == null) {
                    throw new IllegalStateException("Unknown block in structure " + id + ": " + palette.get(paletteIndex));
                }

                resolvedBlocks.add(new ResolvedBlock(entry.get(0), entry.get(1), entry.get(2), block, entry.get(4)));
            }
        }
        return resolvedBlocks;
    }

    public static final class ResolvedBlock {
        private final int x;
        private final int y;
        private final int z;
        private final Block block;
        private final int meta;

        private ResolvedBlock(int x, int y, int z, Block block, int meta) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
            this.meta = meta;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public IBlockState getState() {
            return block.getStateFromMeta(meta);
        }

        public boolean matches(IBlockState actual) {
            return actual != null && actual.getBlock() == block && block.getMetaFromState(actual) == meta;
        }
    }
}
