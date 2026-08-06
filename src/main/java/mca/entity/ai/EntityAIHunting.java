package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

import java.util.Comparator;
import java.util.Optional;

public class EntityAIHunting extends AbstractEntityAIChore {
    private int ticks = 0;
    private int nextAction = 0;
    private int attackCooldown;
    private int retryCooldown;
    private boolean taskActive;
    private EntityAnimal target = null;

    public EntityAIHunting(EntityVillagerMCA entityIn) {
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
        return canDoChore() && isAssignedChore(EnumChore.HUNT);
    }

    public void updateTask() {
        super.updateTask();
        if (!hasAssigningPlayer()) {
            return;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (villager.inventory.getBestHuntingWeapon().isEmpty()) {
            villager.say(getAssigningPlayer(), "chore.hunting.nosword");
            villager.stopChore();
            return;
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;
                Optional<EntityAnimal> animal = villager.world.getEntitiesWithinAABB(EntityAnimal.class, villager.getEntityBoundingBox().grow(15.0D, 3.0D, 15.0D)).stream()
                        .filter((a) -> !(a instanceof EntityTameable))
                        .filter(EntityAnimal::isEntityAlive)
                        .filter((a) -> !a.isChild())
                        .min(Comparator.comparingDouble(villager::getDistance));

                if (animal.isPresent()) {
                    target = animal.get();
                    villager.getNavigator().setPath(villager.getNavigator().getPathToEntityLiving(target), 0.6F);
                } else {
                    retryCooldown = 50;
                }

                nextAction = 50;
            }
        } else {
            boolean pathSuccess = villager.getNavigator().setPath(villager.getNavigator().getPathToEntityLiving(target), 0.6F);

            if (!pathSuccess || !target.isEntityAlive()) {
                // search for EntityItems around the target and grab them
                villager.world.loadedEntityList.stream()
                        .filter((e) -> e instanceof EntityItem && e.getDistance(target) <= 5.0D)
                        .forEach((item) -> {
                            villager.inventory.addItem(((EntityItem) item).getItem());
                            item.setDead();
                        });
                target = null;
            } else if (villager.getDistance(target) <= 3.5F && attackCooldown <= 0) {
                villager.getNavigator().setPath(villager.getNavigator().getPathToEntityLiving(target), 1.0F);
                villager.swingArm(EnumHand.MAIN_HAND);
                target.attackEntityFrom(DamageSource.causeMobDamage(villager), 6.0F);
                villager.getHeldItem(EnumHand.MAIN_HAND).damageItem(2, villager);
                attackCooldown = 20;
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
                && (target != null || ticks == 0 && retryCooldown == 0)
                && (target == null || target.isEntityAlive())
                && canDoChore()
                && isAssignedChore(EnumChore.HUNT);
    }

    @Override
    public void resetTask() {
        super.resetTask();
        taskActive = false;
        ticks = 0;
        nextAction = 0;
        attackCooldown = 0;
        target = null;
    }
}
