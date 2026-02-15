package mca.entity.ai;

import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;

public class EntityAIGuardAttack extends EntityAIBase {
    private final EntityVillagerMCA villager;
    private final double speedTowardsTarget;
    private float attackRange;
    private int attackInterval;
    
    private EntityLivingBase attackTarget;
    private int attackCooldown;
    private int seeTime;
    private long lastAttackTime;
    
    private static final float DEFAULT_ATTACK_RANGE = 2.5F;
    
    public EntityAIGuardAttack(EntityVillagerMCA villager, double speed) {
        this.villager = villager;
        this.speedTowardsTarget = speed;
        this.attackInterval = MCA.getConfig().guardAttackInterval;
        this.attackRange = DEFAULT_ATTACK_RANGE;
        this.setMutexBits(3);
    }
    
    public EntityAIGuardAttack(EntityVillagerMCA villager, double speed, int interval, float range) {
        this.villager = villager;
        this.speedTowardsTarget = speed;
        this.attackInterval = interval;
        this.attackRange = range;
        this.setMutexBits(3);
    }
    
    @Override
    public boolean shouldExecute() {
        if (villager.getProfessionForge() != ProfessionsMCA.guard) {
            return false;
        }
        
        if (guardTooHurt()) {
            return false;
        }
        
        EntityLivingBase target = getPreferredTarget();
        if (target == null || !target.isEntityAlive()) {
            return false;
        }
        
        this.attackTarget = target;
        return true;
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        if (attackTarget == null || !attackTarget.isEntityAlive()) {
            return false;
        }
        
        if (guardTooHurt()) {
            return false;
        }
        
        if (villager.getDistanceSq(attackTarget) > 256.0D) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public void startExecuting() {
        villager.getNavigator().tryMoveToEntityLiving(attackTarget, speedTowardsTarget);
        attackCooldown = 0;
        seeTime = 0;
    }
    
    @Override
    public void resetTask() {
        attackTarget = null;
        villager.getNavigator().clearPath();
        villager.setAttackTarget(null);
    }
    
    @Override
    public void updateTask() {
        if (attackTarget == null) {
            return;
        }
        
        double distanceSq = villager.getDistanceSq(attackTarget.posX, attackTarget.getEntityBoundingBox().minY, attackTarget.posZ);
        boolean canSee = villager.getEntitySenses().canSee(attackTarget);
        
        if (canSee) {
            seeTime++;
        } else {
            seeTime = 0;
        }
        
        if (distanceSq <= getAttackReachSq(attackTarget) && seeTime >= 20) {
            tryAttack();
        } else if (distanceSq > getAttackReachSq(attackTarget)) {
            villager.getNavigator().tryMoveToEntityLiving(attackTarget, speedTowardsTarget);
        }
        
        villager.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);
        
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }
    
    private void tryAttack() {
        if (attackCooldown > 0) {
            return;
        }
        
        if (villager.getEntitySenses().canSee(attackTarget)) {
            villager.swingArm(EnumHand.MAIN_HAND);
            villager.attackEntityAsMob(attackTarget);
            attackCooldown = attackInterval;
            lastAttackTime = villager.world.getTotalWorldTime();
            
            if (attackTarget.isDead || attackTarget.getHealth() <= 0.0F) {
                onTargetKilled();
            }
        }
    }
    
    private void onTargetKilled() {
        if (villager.getRNG().nextFloat() < 0.3F) {
            sendMessage("villager.kill");
        }
    }
    
    private void sendMessage(String key) {
        if (!villager.world.isRemote) {
            double range = 16.0D;
            for (EntityPlayer player : villager.world.playerEntities) {
                if (player.getDistanceSq(villager) <= range * range) {
                    player.sendMessage(new TextComponentTranslation(key, villager.getName()));
                }
            }
        }
    }
    
    private boolean guardTooHurt() {
        return villager.getHealth() < villager.getMaxHealth() * MCA.getConfig().guardRetreatHealthThreshold;
    }
    
    private EntityLivingBase getPreferredTarget() {
        GuardEnemiesSensor sensor = villager.getGuardSensor();
        if (sensor != null && sensor.hasEnemy()) {
            return sensor.getNearestEnemy();
        }
        
        return villager.getAttackTarget();
    }
    
    private double getAttackReachSq(EntityLivingBase target) {
        double reach = villager.width * 2.0F * villager.width * 2.0F + target.width;
        reach += attackRange;
        return reach;
    }
    
    public static boolean isPreferredTarget(EntityVillagerMCA guard, EntityLivingBase target) {
        if (guard == null || target == null) {
            return false;
        }
        
        GuardEnemiesSensor sensor = guard.getGuardSensor();
        if (sensor != null && sensor.hasEnemy()) {
            return sensor.getNearestEnemy() == target;
        }
        
        return guard.getAttackTarget() == target;
    }
}
