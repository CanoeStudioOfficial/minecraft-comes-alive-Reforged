package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.util.RangedWeaponUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.item.ItemBow;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

public class EntityAIArcherGuard extends EntityAIBase {
    private static final double EMERGENCY_DISTANCE_SQUARED = 12.25D;
    private static final double KITE_ENTER_DISTANCE_SQUARED = 36.0D;
    private static final double KITE_EXIT_DISTANCE_SQUARED = 81.0D;
    private static final int LOST_SIGHT_CANCEL_TICKS = 60;
    private static final int VISIBLE_TICKS_BEFORE_STRAFE = 20;

    private final EntityVillagerMCA archer;
    private final double moveSpeed;
    private final int attackCooldown;
    private final float maxAttackDistance;
    private final float maxAttackDistanceSquared;

    private EntityLivingBase attackTarget;
    private int attackTime = -1;
    private int seeTime;
    private int repathCooldown;
    private int strafingTime = -1;
    private boolean strafingClockwise;
    private boolean kiting;

    public EntityAIArcherGuard(EntityVillagerMCA archer, double moveSpeed, int attackCooldown, float maxAttackDistance) {
        this.archer = archer;
        this.moveSpeed = moveSpeed;
        this.attackCooldown = attackCooldown;
        this.maxAttackDistance = maxAttackDistance;
        this.maxAttackDistanceSquared = maxAttackDistance * maxAttackDistance;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase target = this.archer.getAttackTarget();
        if (!isValidTarget(target)) {
            return false;
        }

        this.attackTarget = target;
        return isRangedWeaponInMainhand();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return isValidTarget(this.archer.getAttackTarget()) && isRangedWeaponInMainhand();
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        this.archer.setSwingingArms(true);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        this.attackTarget = null;
        this.attackTime = -1;
        this.seeTime = 0;
        this.repathCooldown = 0;
        this.strafingTime = -1;
        this.kiting = false;
        this.archer.setSwingingArms(false);
        this.archer.resetActiveHand();
        this.archer.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        EntityLivingBase target = this.archer.getAttackTarget();
        if (!isValidTarget(target)) {
            return;
        }

        this.attackTarget = target;

        double distanceSquared = this.archer.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
        boolean canSee = this.archer.getEntitySenses().canSee(target);
        updateSeeTime(canSee);
        updateMovement(target, distanceSquared, canSee);
        updateBowAttack(target, distanceSquared, canSee);
    }

    private void updateMovement(EntityLivingBase target, double distanceSquared, boolean canSee) {
        if (this.repathCooldown > 0) {
            this.repathCooldown--;
        }

        if (distanceSquared < EMERGENCY_DISTANCE_SQUARED) {
            this.kiting = true;
            this.strafingTime = -1;
            this.archer.resetActiveHand();
            moveAwayFrom(target, 0.95D);
            return;
        }

        boolean shouldKite = distanceSquared < (this.kiting ? KITE_EXIT_DISTANCE_SQUARED : KITE_ENTER_DISTANCE_SQUARED);
        if (shouldKite) {
            this.kiting = true;
            this.strafingTime = -1;
            moveAwayFrom(target, 0.85D);
            return;
        }

        this.kiting = false;

        if (distanceSquared > this.maxAttackDistanceSquared || this.seeTime < -10) {
            this.strafingTime = -1;
            this.archer.getNavigator().tryMoveToEntityLiving(target, this.moveSpeed);
            this.archer.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            return;
        }

        if (canSee && this.seeTime >= VISIBLE_TICKS_BEFORE_STRAFE) {
            this.archer.getNavigator().clearPath();
            strafeAround(target, distanceSquared);
        } else {
            this.archer.getNavigator().clearPath();
            this.archer.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }
    }

    private void updateBowAttack(EntityLivingBase target, double distanceSquared, boolean canSee) {
        if (this.attackTime > 0) {
            this.attackTime--;
        }

        if (this.archer.isHandActive()) {
            if (!canSee && this.seeTime < -LOST_SIGHT_CANCEL_TICKS) {
                this.archer.resetActiveHand();
            } else if (canSee && this.archer.getItemInUseMaxCount() >= 20) {
                int useTicks = this.archer.getItemInUseMaxCount();
                this.archer.resetActiveHand();
                this.archer.attackEntityWithRangedAttack(target, ItemBow.getArrowVelocity(useTicks));
                this.attackTime = this.attackCooldown;
            }

            return;
        }

        if (!this.kiting && canSee && distanceSquared <= this.maxAttackDistanceSquared && this.attackTime <= 0) {
            this.archer.setActiveHand(EnumHand.MAIN_HAND);
        }
    }

    private void moveAwayFrom(EntityLivingBase target, double speed) {
        this.archer.faceEntity(target, 30.0F, 30.0F);

        if (this.repathCooldown <= 0 || this.archer.getNavigator().noPath()) {
            Vec3d targetPos = new Vec3d(target.posX, target.posY, target.posZ);
            Vec3d away = findUsefulAwayPosition(target, targetPos);

            if (away != null && this.archer.getNavigator().tryMoveToXYZ(away.x, away.y, away.z, speed)) {
                this.repathCooldown = 10;
                return;
            }

            this.repathCooldown = 4;
        }

        this.archer.getMoveHelper().strafe(-0.5F, this.strafingClockwise ? 0.45F : -0.45F);
        if (this.archer.getRNG().nextInt(20) == 0) {
            this.strafingClockwise = !this.strafingClockwise;
        }
    }

    private Vec3d findUsefulAwayPosition(EntityLivingBase target, Vec3d targetPos) {
        double currentDistance = this.archer.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);

        for (int i = 0; i < 4; i++) {
            Vec3d away = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.archer, 12, 5, targetPos);
            if (away != null && away.squareDistanceTo(target.posX, target.posY, target.posZ) > currentDistance + 1.0D) {
                return away;
            }
        }

        return null;
    }

    private void strafeAround(EntityLivingBase target, double distanceSquared) {
        this.strafingTime++;

        if (this.strafingTime >= 20) {
            if (this.archer.getRNG().nextFloat() < 0.3F) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            this.strafingTime = 0;
        }

        float forward = distanceSquared > this.maxAttackDistanceSquared * 0.75F ? 0.4F : 0.0F;
        this.archer.getMoveHelper().strafe(forward, this.strafingClockwise ? 0.45F : -0.45F);
        this.archer.faceEntity(target, 30.0F, 30.0F);
    }

    private void updateSeeTime(boolean canSee) {
        boolean hadLineOfSight = this.seeTime > 0;
        if (canSee != hadLineOfSight) {
            this.seeTime = 0;
        }

        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }
    }

    private boolean isRangedWeaponInMainhand() {
        return RangedWeaponUtil.isMcaRangedWeapon(this.archer.getHeldItemMainhand());
    }

    private static boolean isValidTarget(EntityLivingBase target) {
        return target != null && target.isEntityAlive();
    }
}
