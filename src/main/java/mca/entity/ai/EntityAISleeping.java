package mca.entity.ai;

import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class EntityAISleeping extends AbstractEntityAIChore {
    private int retryCooldown;

    public EntityAISleeping(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (retryCooldown > 0) {
            retryCooldown--;
            return false;
        }

        //let the avoid tasks work
        if (villager.getHealth() < villager.getMaxHealth()) {
            return false;
        }

        long time = villager.world.getWorldTime() % 24000L;
        if (BlockPos.ORIGIN.equals(villager.get(EntityVillagerMCA.BED_POS)) && time < 16000) { //at tick 18000 villager without bed are allowed to automatically choose one
            //wake up if still sleeping
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return false;
        }

        if (villager.getAttackTarget() != null) {
            //wake up, this is a emergency!
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return false;
        }

        if (time > (villager.getProfessionForge() == ProfessionsMCA.guard ? 14000 : 12000) && time < 23000) {
            return true;
        } else {
            //wake up if still sleeping
            if (villager.isSleeping()) {
                villager.stopSleeping();
            }
            return false;
        }
    }

    public boolean shouldContinueExecuting() {
        return shouldExecute() && (!villager.getNavigator().noPath() || villager.isSleeping());
    }

    public void startExecuting() {
        if (villager.isSleeping()) {
            return;
        }

        BlockPos rememberedBed = villager.get(EntityVillagerMCA.BED_POS);
        boolean needsBedSearch = BlockPos.ORIGIN.equals(rememberedBed)
                || villager.getDistanceSq(rememberedBed) < 4.0
                || !isValidBed(rememberedBed);
        if (needsBedSearch) {
            //search for the nearest bed, might be different than before
            BlockPos pos = villager.searchBed();

            if (pos == null) {
                //no bed found, let's forget about the remembered bed
                //TODO: notify the player?
                villager.set(EntityVillagerMCA.BED_POS, BlockPos.ORIGIN);
                retryCooldown = 100;
            } else {
                villager.set(EntityVillagerMCA.BED_POS, pos);
                villager.startSleeping();
            }
        } else {
            if (!villager.moveTowardsBlock(rememberedBed, 0.75)) {
                retryCooldown = 100;
            }
        }
    }

    @Override
    public void resetTask() {
        super.resetTask();
        if (villager.isSleeping()) {
            villager.stopSleeping();
        }
    }

    public void updateTask() {
        if (villager.isSleeping()) {
            villager.setRotationYawHead(0.0f);
            villager.rotationYaw = 0.0f;
        }
    }

    private boolean isValidBed(BlockPos pos) {
        if (BlockPos.ORIGIN.equals(pos) || !villager.world.isBlockLoaded(pos)) {
            return false;
        }

        IBlockState state = villager.world.getBlockState(pos);
        return state.getBlock().isBed(state, villager.world, pos, villager);
    }
}
