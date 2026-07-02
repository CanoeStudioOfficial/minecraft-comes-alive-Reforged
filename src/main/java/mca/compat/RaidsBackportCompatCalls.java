package mca.compat;

import mca.entity.EntityBanditMCA;
import mca.entity.EntityVillagerMCA;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.raids.common.Constants;
import net.smileycorp.raids.common.RaidsContent;
import net.smileycorp.raids.common.raid.Raid;
import net.smileycorp.raids.common.raid.RaidHandler;
import net.smileycorp.raids.common.raid.Raider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class RaidsBackportCompatCalls {
    private static final String CONVERTED_TAG = "MCAConvertedRaidBandit";
    private static final Set<ResourceLocation> CONVERTIBLE_RAIDERS = new HashSet<>(Arrays.asList(
            new ResourceLocation("raids", "pillager"),
            new ResourceLocation("minecraft", "vindication_illager")
    ));

    private RaidsBackportCompatCalls() {
    }

    static void postInit() {
        RaidHandler.addRaider(EntityBanditMCA.class);
    }

    static boolean isRaider(EntityLivingBase entity) {
        return entity instanceof EntityLiving && RaidHandler.isRaider(entity);
    }

    static void convertRaiderToBandit(EntityLivingBase entity) {
        if (!(entity instanceof EntityLiving) || entity instanceof EntityVillagerMCA || entity.ticksExisted < 5) {
            return;
        }

        EntityLiving source = (EntityLiving) entity;
        if (source.getEntityData().getBoolean(CONVERTED_TAG) || !isConvertibleRaider(source)) {
            return;
        }

        Raider sourceRaider = source.getCapability(RaidsContent.RAIDER, null);
        Raid raid = sourceRaider.getCurrentRaid();
        int wave = sourceRaider.getWave();
        boolean wasLeader = sourceRaider.isPatrolLeader() || ItemStack.areItemStacksEqual(source.getItemStackFromSlot(EntityEquipmentSlot.HEAD), Constants.ominousBanner());
        NBTTagCompound patrolData = raid == null ? sourceRaider.writeNBT(new NBTTagCompound()) : null;

        EntityBanditMCA bandit = new EntityBanditMCA(source.world);
        bandit.setPositionAndRotation(source.posX, source.posY, source.posZ, source.rotationYaw, source.rotationPitch);
        bandit.rotationYawHead = source.rotationYawHead;
        bandit.renderYawOffset = source.renderYawOffset;
        bandit.motionX = source.motionX;
        bandit.motionY = source.motionY;
        bandit.motionZ = source.motionZ;
        bandit.setHealth(Math.max(1.0F, Math.min(bandit.getMaxHealth(), source.getHealth())));
        bandit.getEntityData().setBoolean(CONVERTED_TAG, true);

        if (!source.world.spawnEntity(bandit)) {
            return;
        }

        source.getEntityData().setBoolean(CONVERTED_TAG, true);
        bandit.refreshSpecialAI();

        if (raid != null) {
            raid.removeFromRaid(source, true);
            raid.joinRaid(wave, bandit, true);
            if (wasLeader) {
                raid.setLeader(wave, bandit);
            }
        } else if (patrolData != null && bandit.hasCapability(RaidsContent.RAIDER, null)) {
            Raider banditRaider = bandit.getCapability(RaidsContent.RAIDER, null);
            banditRaider.readNBT(patrolData);
            if (wasLeader) {
                banditRaider.setLeader();
            }
        }

        source.setDead();
    }

    private static boolean isConvertibleRaider(EntityLiving source) {
        if (!source.hasCapability(RaidsContent.RAIDER, null)) {
            return false;
        }

        Raider raider = source.getCapability(RaidsContent.RAIDER, null);
        if (raider == null || (!raider.hasActiveRaid() && !raider.isPatrolling())) {
            return false;
        }

        ResourceLocation id = EntityList.getKey(source);
        return id != null && CONVERTIBLE_RAIDERS.contains(id);
    }
}
