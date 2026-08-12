package mca.structure;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import mca.core.minecraft.ItemsMCA;

/** Performs lightweight, server-side recognition for the blueprint catalog. */
public final class McaBlueprintManager {
    private McaBlueprintManager() {
    }

    public static void scan(EntityPlayerMP player) {
        if (!isHoldingBlueprint(player)) {
            return;
        }

        BlockPos playerPos = player.getPosition();
        McaStructure best = null;
        BlockPos bestAnchor = null;
        int bestMatches = 0;

        for (McaStructure structure : McaStructureRegistry.all()) {
            for (int dx = -8; dx <= 8; dx += 4) {
                for (int dz = -8; dz <= 8; dz += 4) {
                    BlockPos anchor = playerPos.add(dx, 0, dz);
                    int matches = sampleMatches(player.world, structure, anchor);
                    if (matches > bestMatches) {
                        bestMatches = matches;
                        best = structure;
                        bestAnchor = anchor;
                    }
                }
            }
        }

        if (best == null || bestMatches < 8) {
            player.sendMessage(new TextComponentTranslation("blueprint.scan.none"));
        } else {
            player.sendMessage(new TextComponentTranslation("blueprint.scan.found", best.getId(), best.getWidth(), best.getHeight(), best.getLength(), bestMatches));
        }
    }

    private static boolean isHoldingBlueprint(EntityPlayerMP player) {
        return player.getHeldItemMainhand().getItem() == ItemsMCA.BLUEPRINT
                || player.getHeldItemMainhand().getItem() == ItemsMCA.BOOK_BLUEPRINT
                || player.getHeldItemOffhand().getItem() == ItemsMCA.BLUEPRINT
                || player.getHeldItemOffhand().getItem() == ItemsMCA.BOOK_BLUEPRINT;
    }

    private static int sampleMatches(World world, McaStructure structure, BlockPos anchor) {
        java.util.List<McaStructure.ResolvedBlock> blocks = structure.getResolvedBlocks();
        int sampleSize = Math.min(32, blocks.size());
        int stride = Math.max(1, blocks.size() / sampleSize);
        int matches = 0;
        for (int i = 0; i < blocks.size() && matches <= sampleSize; i += stride) {
            McaStructure.ResolvedBlock expected = blocks.get(i);
            BlockPos target = new BlockPos(anchor.getX() - structure.getWidth() / 2 + expected.getX(), anchor.getY() - 1 + expected.getY(), anchor.getZ() - structure.getLength() / 2 + expected.getZ());
            if (expected.matches(world.getBlockState(target))) {
                matches++;
            }
        }
        return matches;
    }
}
