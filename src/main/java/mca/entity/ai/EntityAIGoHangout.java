package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import net.minecraft.util.math.BlockPos;

public class EntityAIGoHangout extends AbstractEntityAIChore {
    private boolean atHangout = false;
    private int retryCooldown;
    
    public EntityAIGoHangout(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (retryCooldown > 0) {
            retryCooldown--;
            return false;
        }

        if (villager.getAttackTarget() != null || BlockPos.ORIGIN.equals(villager.getHangout())) {
            return false; //no workplace
        }

        //no time, has to work
        if (EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) != EnumChore.NONE) {
            return false;
        }

        long time = villager.world.getWorldTime() % 24000L;

        if (time < 9000 || time > 11000) {
            //spare time is over, villager will start going home
            atHangout = false;
            return false;
        }

        double validArea = 64.0D; //allows 8 blocks radius to stay
        double distance = villager.getDistanceSq(villager.getHangout());

        if (!atHangout) {
            if (distance < 9.0) {
                //arrived at hangout
                atHangout = true;
            } else {
                //did not reach workplace for today -> shrink valid area so the villager gathers clearly at his workplace
                validArea = 4.0D;
            }
        }

        return distance > validArea;
    }

    public boolean shouldContinueExecuting() {
        return isHangoutTime() && villager.getAttackTarget() == null && !villager.getNavigator().noPath();
    }

    public void startExecuting() {
        if (!villager.moveTowardsBlock(villager.getHangout())) {
            retryCooldown = 100;
        }
    }

    public void updateTask() {

    }

    @Override
    public void resetTask() {
        super.resetTask();
        atHangout = false;
    }

    private boolean isHangoutTime() {
        long time = villager.world.getWorldTime() % 24000L;
        return time >= 9000L && time <= 11000L;
    }
}
