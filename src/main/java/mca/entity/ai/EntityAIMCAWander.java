package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumMoveState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

public class EntityAIMCAWander extends EntityAIBase {
    private final EntityCreature entity;
    private final double speed;
    private final int executionChance;

    private double x;
    private double y;
    private double z;

    public EntityAIMCAWander(EntityCreature entity, double speed, int executionChance) {
        this.entity = entity;
        this.speed = speed;
        this.executionChance = executionChance;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!canIdleMove() || !entity.getNavigator().noPath() || entity.getRNG().nextInt(executionChance) != 0) {
            return false;
        }

        Vec3d target = RandomPositionGenerator.getLandPos(entity, 10, 7);
        if (target == null) {
            target = RandomPositionGenerator.findRandomTarget(entity, 10, 7);
        }

        if (target == null) {
            return false;
        }

        x = target.x;
        y = target.y;
        z = target.z;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return canIdleMove() && !entity.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        entity.getNavigator().tryMoveToXYZ(x, y, z, speed);
    }

    private boolean canIdleMove() {
        if (entity.getAttackTarget() != null) {
            return false;
        }

        if (entity instanceof EntityVillagerMCA) {
            EntityVillagerMCA villager = (EntityVillagerMCA) entity;
            return !villager.isSleeping()
                    && EnumMoveState.byId(villager.get(EntityVillagerMCA.MOVE_STATE)) == EnumMoveState.MOVE;
        }

        return true;
    }
}
