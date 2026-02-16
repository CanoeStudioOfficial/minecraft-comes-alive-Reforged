package mca.entity.ai;

import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityAIPatrolVillage extends EntityAIBase {
    private final EntityCreature entity;

    private Vec3d targetPosition;
    private int waitTimer;

    public EntityAIPatrolVillage(EntityCreature creature) {
        this.entity = creature;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (!MCA.getConfig().guardPatrolEnabled) {
            return false;
        }

        if (this.entity instanceof EntityVillagerMCA) {
            EntityVillagerMCA villager = (EntityVillagerMCA) this.entity;
            if (villager.getProfessionForge() != ProfessionsMCA.guard) {
                return false;
            }
        }

        if (this.entity.getAttackTarget() != null) {
            return false;
        }

        int executionChance = MCA.getConfig().guardPatrolWaitTime;

        if (this.entity.getNavigator().noPath()) {
            if (this.entity.getRNG().nextInt(executionChance) != 0) {
                return this.targetPosition != null;
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.entity.getNavigator().noPath() && this.waitTimer > 0;
    }

    @Override
    public void startExecuting() {
        if (this.targetPosition == null) {
            this.findNewPosition();
        }

        if (this.targetPosition != null) {
            this.entity.getNavigator().tryMoveToXYZ(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, MCA.getConfig().guardPatrolSpeed);
        }
    }

    @Override
    public void updateTask() {
        if (this.entity.getNavigator().noPath()) {
            this.waitTimer--;

            if (this.waitTimer <= 0) {
                this.findNewPosition();
            }
        }
    }

    private void findNewPosition() {
        World world = this.entity.world;
        Village village = this.getNearestVillage();

        if (village != null) {
            BlockPos center = village.getCenter();
            int villageRadius = village.getVillageRadius();

            Vec3d randomPos = this.getRandomPositionInVillage(center, villageRadius);
            if (randomPos != null && this.isPositionValid(randomPos)) {
                this.targetPosition = randomPos;
                this.waitTimer = 100 + this.entity.getRNG().nextInt(100);
                this.entity.getNavigator().tryMoveToXYZ(this.targetPosition.x, this.targetPosition.y, this.targetPosition.z, MCA.getConfig().guardPatrolSpeed);
            }
        } else {
            this.targetPosition = null;
        }
    }

    private Village getNearestVillage() {
        World world = this.entity.world;
        if (world == null || world.getVillageCollection() == null) {
            return null;
        }
        return world.getVillageCollection().getNearestVillage(this.entity.getPosition(), MCA.getConfig().guardPatrolRadius);
    }

    private Vec3d getRandomPositionInVillage(BlockPos center, int villageRadius) {
        int x = center.getX() + this.entity.getRNG().nextInt(villageRadius * 2) - villageRadius;
        int z = center.getZ() + this.entity.getRNG().nextInt(villageRadius * 2) - villageRadius;
        int y = this.entity.world.getHeight(new BlockPos(x, 0, z)).getY();

        return new Vec3d(x, y, z);
    }

    private boolean isPositionValid(Vec3d pos) {
        if (pos.y < 0 || pos.y > 256) {
            return false;
        }

        BlockPos blockPos = new BlockPos(pos);
        return !this.entity.world.getBlockState(blockPos).isFullBlock();
    }
}
