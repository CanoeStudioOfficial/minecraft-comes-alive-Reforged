package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import mca.util.Util;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;

public class EntityAIFishing extends AbstractEntityAIChore {
    private BlockPos targetWater;
    private boolean hasCastRod;
    private int ticks;
    private int nextCatchTicks;
    private int retryCooldown;
    private boolean taskActive;

    public EntityAIFishing(EntityVillagerMCA entityIn) {
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
        return canDoChore() && isAssignedChore(EnumChore.FISH);
    }

    public void updateTask() {
        super.updateTask();
        if (!hasAssigningPlayer()) {
            return;
        }

        if (!villager.inventory.contains(ItemFishingRod.class)) {
            villager.say(getAssigningPlayer(), "chore.fishing.norod");
            villager.stopChore();
            return;
        }

        if (targetWater == null || villager.world.getBlockState(targetWater).getBlock() != Blocks.WATER) {
            List<BlockPos> nearbyStaticLiquid = Util.getNearbyBlocks(villager.getPos(), villager.world, BlockStaticLiquid.class, 12, 3);
            targetWater = nearbyStaticLiquid.stream()
                    .filter((p) -> villager.world.getBlockState(p).getBlock() == Blocks.WATER)
                    .min(Comparator.comparingDouble(villager::getDistanceSq)).orElse(null);
            if (targetWater == null) {
                retryCooldown = 100;
            }
        } else if (villager.getDistanceSq(targetWater) > 5.0D) {
            if (!villager.getNavigator().setPath(villager.getNavigator().getPathToPos(targetWater), 0.8D)
                    && !villager.attemptTeleport(targetWater.getX(), targetWater.getY(), targetWater.getZ())) {
                targetWater = null;
                retryCooldown = 100;
            }
        } else {
            villager.getNavigator().clearPath();

            if (!hasCastRod) {
                villager.swingArm(EnumHand.MAIN_HAND);
                hasCastRod = true;
                nextCatchTicks = villager.world.rand.nextInt(200) + 200;
            }

            ticks++;

            if (ticks >= nextCatchTicks) {
                if (villager.world.rand.nextFloat() >= 0.35F) {
                    int typesSize = ItemFishFood.FishType.values().length;
                    ItemFishFood.FishType type = ItemFishFood.FishType.values()[villager.world.rand.nextInt(typesSize)];
                    ItemStack stack = new ItemStack(Items.FISH, 1, type.getMetadata());

                    villager.swingArm(EnumHand.MAIN_HAND);
                    villager.inventory.addItem(stack);
                    villager.getHeldItem(EnumHand.MAIN_HAND).damageItem(2, villager);
                }
                ticks = 0;
                nextCatchTicks = villager.world.rand.nextInt(200) + 200;
            }
        }
    }

    @Override
    public void startExecuting() {
        taskActive = true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return taskActive
                && (targetWater != null || ticks == 0 && retryCooldown == 0)
                && canDoChore()
                && isAssignedChore(EnumChore.FISH);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        taskActive = false;
        targetWater = null;
        hasCastRod = false;
        ticks = 0;
        nextCatchTicks = 0;
    }
}
