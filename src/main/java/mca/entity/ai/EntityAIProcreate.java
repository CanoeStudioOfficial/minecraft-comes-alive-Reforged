package mca.entity.ai;

import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ItemsMCA;
import mca.entity.EntityVillagerMCA;
import mca.entity.data.PlayerSaveData;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;

public class EntityAIProcreate extends EntityAIBase {
    private final EntityVillagerMCA villager;
    public int procreateTimer;

    public EntityAIProcreate(EntityVillagerMCA villager) {
        this.villager = villager;
    }

    @Override
    public boolean shouldExecute() {
        return villager.get(EntityVillagerMCA.IS_PROCREATING);
    }

    @Override
    public void updateTask() {
        if (procreateTimer % 5 == 0) villager.spawnParticles(EnumParticleTypes.HEART);

        if (--procreateTimer <= 0) {
            villager.set(EntityVillagerMCA.IS_PROCREATING, false);

            EntityPlayer spousePlayer = villager.world.getPlayerEntityByUUID(villager.get(EntityVillagerMCA.SPOUSE_UUID).or(Constants.ZERO_UUID));
            if (spousePlayer != null) {
                villager.world.playSound(null, villager.posX, villager.posY, villager.posZ, SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                
                PlayerSaveData spouseData = PlayerSaveData.get(spousePlayer);
                ItemStack babyStack = new ItemStack(villager.getRNG().nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
                NBTTagCompound babyNbt = new NBTTagCompound();
                
                // Inherit genetics and traits
                NBTTagCompound parent1Genetics = villager.get(EntityVillagerMCA.GENETICS);
                NBTTagCompound parent1Traits = villager.get(EntityVillagerMCA.TRAITS);
                NBTTagCompound parent2Genetics = spouseData.getGenetics();
                NBTTagCompound parent2Traits = spouseData.getTraits();
                
                babyNbt.setTag("parent1Genetics", parent1Genetics);
                babyNbt.setTag("parent1Traits", parent1Traits);
                babyNbt.setTag("parent2Genetics", parent2Genetics);
                babyNbt.setTag("parent2Traits", parent2Traits);
                babyStack.setTagCompound(babyNbt);

                spousePlayer.inventory.addItemStackToInventory(babyStack);
                spouseData.setBabyPresent(true);

                if (villager.getRNG().nextFloat() < MCA.getConfig().chanceToHaveTwins / 100) {
                    ItemStack twinStack = babyStack.copy();
                    // Optional: different gender for twin
                    if (villager.getRNG().nextBoolean()) {
                        twinStack = new ItemStack(villager.getRNG().nextBoolean() ? ItemsMCA.BABY_BOY : ItemsMCA.BABY_GIRL);
                        twinStack.setTagCompound(babyNbt.copy());
                    }
                    spousePlayer.inventory.addItemStackToInventory(twinStack);
                }
            }
        }
    }
}
