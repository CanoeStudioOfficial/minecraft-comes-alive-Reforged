package mca.entity.ai;

import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

public class EntityAIGuardBowAttack extends EntityAIBase {
    private final EntityVillagerMCA villager;
    private final double moveSpeed;
    private int attackInterval;
    private int attackRange;
    
    private EntityLivingBase attackTarget;
    private int attackCooldown;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime;
    
    public EntityAIGuardBowAttack(EntityVillagerMCA villager, double speed, int interval, int range) {
        this.villager = villager;
        this.moveSpeed = speed;
        this.attackInterval = interval;
        this.attackRange = range;
        this.setMutexBits(3);
    }
    
    public EntityAIGuardBowAttack(EntityVillagerMCA villager, double speed) {
        this.villager = villager;
        this.moveSpeed = speed;
        this.attackInterval = MCA.getConfig().guardBowAttackInterval;
        this.attackRange = MCA.getConfig().guardBowAttackRange;
        this.setMutexBits(3);
    }
    
    @Override
    public boolean shouldExecute() {
        if (villager.getProfessionForge() != ProfessionsMCA.guard) {
            return false;
        }
        
        if (!isHoldingBow()) {
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
        
        if (!isHoldingBow()) {
            return false;
        }
        
        if (guardTooHurt()) {
            return false;
        }
        
        double distanceSq = villager.getDistanceSq(attackTarget);
        return distanceSq <= (attackRange * attackRange * 1.5);
    }
    
    @Override
    public void startExecuting() {
        attackCooldown = 0;
        seeTime = 0;
        strafingTime = 0;
        strafingClockwise = villager.getRNG().nextBoolean();
        strafingBackwards = villager.getRNG().nextBoolean();
    }
    
    @Override
    public void resetTask() {
        attackTarget = null;
        attackCooldown = 0;
        seeTime = 0;
        villager.getNavigator().clearPath();
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
        
        if (distanceSq <= (attackRange * attackRange) && seeTime >= 20) {
            if (attackCooldown <= 0) {
                tryShoot();
            }
            
            doStrafing();
        } else {
            villager.getNavigator().tryMoveToEntityLiving(attackTarget, moveSpeed);
        }
        
        villager.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);
        
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }
    
    private void doStrafing() {
        strafingTime++;
        
        if (strafingTime >= 40) {
            if (villager.getRNG().nextFloat() < 0.3) {
                strafingClockwise = !strafingClockwise;
            }
            if (villager.getRNG().nextFloat() < 0.3) {
                strafingBackwards = !strafingBackwards;
            }
            strafingTime = 0;
        }
        
        if (villager.getNavigator().noPath()) {
            double distance = villager.getDistanceSq(attackTarget);
            float strafeSpeed = 0.5F;
            
            if (distance > attackRange * attackRange * 0.75) {
                strafingBackwards = false;
            } else if (distance < attackRange * attackRange * 0.25) {
                strafingBackwards = true;
            }
            
            villager.getMoveHelper().strafe(
                    strafingBackwards ? -strafeSpeed : strafeSpeed,
                    strafingClockwise ? strafeSpeed : -strafeSpeed
            );
        }
    }
    
    private void tryShoot() {
        if (!isHoldingBow()) {
            return;
        }
        
        float charge = getArrowVelocity();
        if (charge >= 0.1F) {
            shootArrow(charge);
            attackCooldown = attackInterval;
        }
    }
    
    private void shootArrow(float charge) {
        EntityTippedArrow arrow = new EntityTippedArrow(villager.world, villager);
        
        double d0 = attackTarget.posX - villager.posX;
        double d1 = attackTarget.getEntityBoundingBox().minY + attackTarget.height / 3.0F - arrow.posY;
        double d2 = attackTarget.posZ - villager.posZ;
        
        double distance = MathHelper.sqrt(d0 * d0 + d2 * d2);
        float velocity = MathHelper.sqrt(distance * distance + d1 * d1) * 0.2F;
        
        arrow.shoot(d0, d1 + velocity, d2, 1.6F, 10.0F);
        
        arrow.setDamage(arrow.getDamage() * 0.5 + 0.5);
        
        villager.world.spawnEntity(arrow);
        
        villager.swingArm(EnumHand.MAIN_HAND);
    }
    
    private float getArrowVelocity() {
        return 1.0F;
    }
    
    private boolean isHoldingBow() {
        ItemStack mainHand = villager.getHeldItemMainhand();
        return mainHand.getItem() instanceof ItemBow || mainHand.getItem() == Items.BOW;
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
}
