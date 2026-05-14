package mca.entity.ai;

import mca.entity.EntityVillagerMCA;
import mca.enums.EnumChore;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EntityAIHunting extends AbstractEntityAIChore {
    private int ticks = 0;
    private int nextAction = 0;
    private EntityAnimal target = null;

    public EntityAIHunting(EntityVillagerMCA entityIn) {
        super(entityIn);
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (villager.getHealth() < villager.getMaxHealth()) {
            villager.stopChore();
        }
        return EnumChore.byId(villager.get(EntityVillagerMCA.ACTIVE_CHORE)) == EnumChore.HUNT;
    }

    public void updateTask() {
        super.updateTask();

        if (!villager.inventory.contains(ItemSword.class)) {
            villager.say(getAssigningPlayer(), "chore.hunting.nosword");
            villager.stopChore();
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;
                Optional<EntityAnimal> animal = villager.world.getEntitiesWithinAABB(EntityAnimal.class, villager.getEntityBoundingBox().grow(15.0D, 3.0D, 15.0D)).stream()
                        .filter((a) -> !(a instanceof EntityTameable))
                        .min(Comparator.comparingDouble(villager::getDistance));

                if (animal.isPresent()) {
                    target = animal.get();
                    target.getNavigator().setPath(target.getNavigator().getPathToEntityLiving(villager), 1.0F);
                }

                nextAction = 300;
            }
        } else {
            boolean pathSuccess = villager.getNavigator().setPath(villager.getNavigator().getPathToEntityLiving(target), 0.6F);

            if (!pathSuccess || target.isDead) {
                List<EntityItem> nearbyItems = villager.world.getEntitiesWithinAABB(EntityItem.class, target.getEntityBoundingBox().grow(5.0D));
                for (EntityItem item : nearbyItems) {
                    villager.inventory.addItem(item.getItem());
                    item.setDead();
                }
                target = null;
            } else if (villager.getDistance(target) <= 3.5F) {
                villager.getNavigator().setPath(villager.getNavigator().getPathToEntityLiving(target), 1.0F);
                villager.swingArm(EnumHand.MAIN_HAND);
                target.attackEntityFrom(DamageSource.causeMobDamage(villager), 6.0F);
                villager.getHeldItem(EnumHand.MAIN_HAND).damageItem(2, villager);
            }
        }
    }
}