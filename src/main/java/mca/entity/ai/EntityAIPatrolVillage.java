package mca.entity.ai;

import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityAIPatrolVillage extends EntityAIBase {
    private final EntityCreature entity;
    private final EntityVillagerMCA villager;
    
    private Vec3d targetPosition;
    private BlockPos targetBlockPos;
    private int waitTimer;
    private int failedPathAttempts;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    private long lastPatrolTime;
    private static final long PATROL_COOLDOWN = 200;
    
    public EntityAIPatrolVillage(EntityCreature creature) {
        this.entity = creature;
        this.villager = creature instanceof EntityVillagerMCA ? (EntityVillagerMCA) creature : null;
        this.setMutexBits(1);
    }
    
    @Override
    public boolean shouldExecute() {
        if (!MCA.getConfig().guardPatrolEnabled) {
            return false;
        }
        
        if (villager == null || villager.getProfessionForge() != ProfessionsMCA.guard) {
            return false;
        }
        
        if (entity.getAttackTarget() != null) {
            return false;
        }
        
        if (isInteracting()) {
            return false;
        }
        
        if (villager.getGuardSensor() != null && villager.getGuardSensor().hasEnemy()) {
            return false;
        }
        
        if (!entity.getNavigator().noPath()) {
            return false;
        }
        
        if (entity.ticksExisted - lastPatrolTime < PATROL_COOLDOWN) {
            return false;
        }
        
        int executionChance = MCA.getConfig().guardPatrolWaitTime;
        return entity.getRNG().nextInt(executionChance) == 0;
    }
    
    private boolean isInteracting() {
        if (villager == null) return false;
        return villager.getMoveState() != mca.enums.EnumMoveState.MOVE;
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        if (entity.getAttackTarget() != null) {
            return false;
        }
        
        if (villager != null && villager.getGuardSensor() != null && villager.getGuardSensor().hasEnemy()) {
            return false;
        }
        
        return entity.getNavigator().noPath() && waitTimer > 0;
    }
    
    @Override
    public void startExecuting() {
        failedPathAttempts = 0;
        findNewPosition();
        lastPatrolTime = entity.ticksExisted;
    }
    
    @Override
    public void resetTask() {
        targetPosition = null;
        targetBlockPos = null;
        waitTimer = 0;
        failedPathAttempts = 0;
    }
    
    @Override
    public void updateTask() {
        if (entity.getNavigator().noPath()) {
            waitTimer--;
            
            if (waitTimer <= 0) {
                findNewPosition();
            }
        }
    }
    
    private void findNewPosition() {
        Village village = getNearestVillage();
        
        if (village == null) {
            targetPosition = null;
            return;
        }
        
        BlockPos center = village.getCenter();
        int villageRadius = Math.max(village.getVillageRadius(), 32);
        
        for (int attempt = 0; attempt < 5; attempt++) {
            Vec3d randomPos = getRandomPositionInVillage(center, villageRadius);
            
            if (randomPos != null && isPositionValid(randomPos)) {
                if (tryMoveToPosition(randomPos)) {
                    targetPosition = randomPos;
                    targetBlockPos = new BlockPos(randomPos);
                    waitTimer = 100 + entity.getRNG().nextInt(100);
                    return;
                }
            }
        }
        
        failedPathAttempts++;
        if (failedPathAttempts >= MAX_FAILED_ATTEMPTS) {
            targetPosition = null;
            waitTimer = 200;
        }
    }
    
    private boolean tryMoveToPosition(Vec3d pos) {
        PathNavigate navigator = entity.getNavigator();
        double speed = MCA.getConfig().guardPatrolSpeed;
        
        Path path = navigator.getPathToXYZ(pos.x, pos.y, pos.z);
        if (path != null) {
            navigator.setPath(path, speed);
            return true;
        }
        return false;
    }
    
    @Nullable
    private Village getNearestVillage() {
        World world = entity.world;
        if (world == null || world.getVillageCollection() == null) {
            return null;
        }
        return world.getVillageCollection().getNearestVillage(entity.getPosition(), MCA.getConfig().guardPatrolRadius);
    }
    
    @Nullable
    private Vec3d getRandomPositionInVillage(BlockPos center, int villageRadius) {
        Random rand = entity.getRNG();
        
        double angle = rand.nextDouble() * 2 * Math.PI;
        double distance = rand.nextDouble() * villageRadius;
        
        int x = center.getX() + (int) (Math.cos(angle) * distance);
        int z = center.getZ() + (int) (Math.sin(angle) * distance);
        int y = entity.world.getHeight(new BlockPos(x, 0, z)).getY();
        
        if (y <= 0) {
            return null;
        }
        
        return new Vec3d(x + 0.5, y, z + 0.5);
    }
    
    private boolean isPositionValid(Vec3d pos) {
        if (pos.y < 0 || pos.y > 256) {
            return false;
        }
        
        BlockPos blockPos = new BlockPos(pos.x, pos.y, pos.z);
        
        if (entity.world.getBlockState(blockPos).isFullBlock()) {
            return false;
        }
        
        BlockPos groundPos = blockPos.down();
        if (!entity.world.getBlockState(groundPos).isFullBlock()) {
            return false;
        }
        
        double distanceSq = entity.getDistanceSq(pos.x, pos.y, pos.z);
        if (distanceSq < 4.0) {
            return false;
        }
        
        return true;
    }
}
