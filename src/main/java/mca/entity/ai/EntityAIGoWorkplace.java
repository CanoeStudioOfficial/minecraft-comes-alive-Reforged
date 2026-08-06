package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import net.minecraft.util.math.BlockPos;

public class EntityAIGoWorkplace extends AbstractEntityAIChore {
    private boolean atWork = false;
    private int retryCooldown;

    public EntityAIGoWorkplace(EntityVillagerMCA villagerIn) {
        super(villagerIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (retryCooldown > 0) {
            retryCooldown--;
            return false;
        }

        if (villager.getAttackTarget() != null || BlockPos.ORIGIN.equals(villager.getWorkplace()) || villager.world.isRaining()) {
            return false; //no workplace or it is raining
        }

        long time = villager.world.getWorldTime() % 24000L;

        if (time < 4000 || time > 7000) {
            //work is over, villager will start spreading
            atWork = false;
            return false;
        }

        double validArea = 576.0D; //allows 24 blocks radius to work
        double distance = villager.getDistanceSq(villager.getWorkplace());

        if (!atWork) {
            if (distance < 9.0) {
                //arrived at workplace
                atWork = true;
            } else {
                //did not reach workplace for today -> shrink valid area so the villager gathers clearly at his workplace
                validArea = 4.0D;
            }
        }

        return distance > validArea;
    }

    public boolean shouldContinueExecuting() {
        return isWorkTime() && villager.getAttackTarget() == null && !villager.world.isRaining() && !villager.getNavigator().noPath();
    }

    public void startExecuting() {
        //MCA.getLog().info(villager.getName() + " goes to work");
        if (!villager.moveTowardsBlock(villager.getWorkplace())) {
            retryCooldown = 100;
        }
    }

    public void updateTask() {

    }

    @Override
    public void resetTask() {
        super.resetTask();
        atWork = false;
    }

    private boolean isWorkTime() {
        long time = villager.world.getWorldTime() % 24000L;
        return time >= 4000L && time <= 7000L;
    }
}
