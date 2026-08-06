package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class EntityAIChopping extends AbstractEntityAIChore {
    private int chopTicks;
    private int retryCooldown;
    private boolean taskActive;
    private BlockPos targetTree;

    public EntityAIChopping(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (retryCooldown > 0) {
            retryCooldown--;
            return false;
        }
        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }
        return canDoChore() && isAssignedChore(EnumChore.CHOP);
    }

    public void updateTask() {
        super.updateTask();
        if (!hasAssigningPlayer()) {
            return;
        }

        if (!villager.inventory.contains(ItemAxe.class)) {
            villager.say(getAssigningPlayer(), "chore.chopping.noaxe");
            villager.stopChore();
            return;
        }
        if (targetTree == null) {
            List<BlockPos> nearbyLogs = Util.getNearbyBlocks(villager.getPosition(), villager.world, BlockLog.class, 10, 5);
            List<BlockPos> nearbyTrees = new ArrayList<>();

            // valid "trees" are logs on the ground with multiple leaves around/above them
            nearbyLogs.stream()
                    .filter(log -> {
                        IBlockState down = villager.world.getBlockState(log.down());
                        boolean isOnTreeBase = down.getBlock() == Blocks.GRASS || down.getBlock() == Blocks.DIRT;
                        
                        if (isOnTreeBase) {
                            List<BlockPos> leaves = Util.getNearbyBlocks(log, villager.world, BlockLeaves.class, 3, 8);
                            return leaves.size() >= 3; // Must have at least 3 leaf blocks nearby to be considered a tree
                        }
                        return false;
                    })
                    .forEach(nearbyTrees::add);
            targetTree = Util.getNearestPoint(villager.getPosition(), nearbyTrees);
            if (targetTree == null) {
                retryCooldown = 100;
            }
            return;
        }
        double distance = Math.sqrt(villager.getDistanceSq(targetTree));
        if (distance >= 4.0D) {
            if (!villager.getNavigator().setPath(villager.getNavigator().getPathToPos(targetTree), 0.5D)
                    && !villager.attemptTeleport(targetTree.getX(), targetTree.getY(), targetTree.getZ())) {
                targetTree = null;
                chopTicks = 0;
                retryCooldown = 100;
            }
        } else {
            IBlockState state = villager.world.getBlockState(targetTree);
            if (state.getBlock() instanceof BlockLog) {
                BlockLog log = (BlockLog) state.getBlock();
                villager.swingArm(EnumHand.MAIN_HAND);
                chopTicks++;

                if (chopTicks >= 80) {
                    chopTicks = 0;
                    if (villager.world.rand.nextFloat() >= 0.90) {
                        destroyTree(targetTree);
                    } else {
                        villager.inventory.addItem(new ItemStack(log, 1));
                        villager.getHeldItem(EnumHand.MAIN_HAND).damageItem(2, villager);
                    }
                }
            } else targetTree = null;
        }
    }

    @Override
    public void startExecuting() {
        taskActive = true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return taskActive
                && (targetTree != null || chopTicks == 0 && retryCooldown == 0)
                && canDoChore()
                && isAssignedChore(EnumChore.CHOP);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        taskActive = false;
        chopTicks = 0;
        targetTree = null;
    }

    private void destroyTree(BlockPos origin) {
        BlockPos pos = origin;
        while (villager.world.getBlockState(pos).getBlock() instanceof BlockLog) {
            BlockLog log = (BlockLog) villager.world.getBlockState(pos).getBlock();
            villager.world.setBlockToAir(pos);
            villager.inventory.addItem(new ItemStack(log, 1));
            villager.getHeldItem(EnumHand.MAIN_HAND).damageItem(1, villager);
            pos = pos.add(0, 1, 0);
        }
    }
}
