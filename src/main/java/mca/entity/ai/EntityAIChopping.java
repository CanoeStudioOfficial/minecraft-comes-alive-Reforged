package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityAIChopping extends AbstractEntityAIChore {
    private int chopTicks;
    private BlockPos targetTree;

    public EntityAIChopping(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }
        return EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) == EnumChore.CHOP;
    }

    public void updateTask() {
        super.updateTask();

        if (!villager.inventory.contains(ItemAxe.class)) {
            villager.say(getAssigningPlayer(), "chore.chopping.noaxe");
            villager.stopChore();
        }
        if (targetTree == null) {
            List<BlockPos> nearbyLogs = Util.getNearbyBlocks(villager.getPosition(), villager.world, BlockLog.class, 10, 5);
            List<BlockPos> nearbyLeaves = Util.getNearbyBlocks(villager.getPosition(), villager.world, BlockLeaves.class, 10, 13);
            Set<BlockPos> leafSet = new HashSet<>(nearbyLeaves);
            List<BlockPos> nearbyTrees = new ArrayList<>();

            for (BlockPos log : nearbyLogs) {
                IBlockState down = villager.world.getBlockState(log.down());
                if (down.getBlock() != Blocks.GRASS && down.getBlock() != Blocks.DIRT) continue;

                int leafCount = 0;
                for (BlockPos leaf : nearbyLeaves) {
                    if (Math.abs(leaf.getX() - log.getX()) <= 3
                            && Math.abs(leaf.getZ() - log.getZ()) <= 3
                            && leaf.getY() >= log.getY()
                            && leaf.getY() - log.getY() <= 8) {
                        leafCount++;
                        if (leafCount >= 3) break;
                    }
                }
                if (leafCount >= 3) {
                    nearbyTrees.add(log);
                }
            }
            targetTree = Util.getNearestPoint(villager.getPosition(), nearbyTrees);
            return;
        }
        double distance = Math.sqrt(villager.getDistanceSq(targetTree));
        if (distance >= 4.0D) villager.getNavigator().setPath(villager.getNavigator().getPathToPos(targetTree), 0.5D);
        else {
            IBlockState state = villager.world.getBlockState(targetTree);
            Block block = state.getBlock();
            if (block instanceof BlockLog) {
                villager.swingArm(EnumHand.MAIN_HAND);
                chopTicks++;

                if (chopTicks >= 80) {
                    chopTicks = 0;
                    villager.inventory.addItem(new ItemStack(block, 1));
                    villager.getHeldItem(EnumHand.MAIN_HAND).damageItem(2, villager);
                    if (villager.world.rand.nextFloat() >= 0.90) destroyTree(targetTree);
                }
            } else targetTree = null;
        }
    }

    private void destroyTree(BlockPos origin) {
        BlockPos pos = origin;
        while (villager.world.getBlockState(pos).getBlock() instanceof BlockLog) {
            villager.world.setBlockToAir(pos);
            pos = pos.add(0, 1, 0);
        }
    }
}