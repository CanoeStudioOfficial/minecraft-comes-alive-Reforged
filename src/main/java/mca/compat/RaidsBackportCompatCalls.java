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
import net.minecraft.world.WorldServer;
import net.smileycorp.raids.common.Constants;
import net.smileycorp.raids.common.RaidsContent;
import net.smileycorp.raids.common.raid.Raid;
import net.smileycorp.raids.common.raid.RaidHandler;
import net.smileycorp.raids.common.raid.Raider;
import net.smileycorp.raids.common.world.WorldDataOutposts;
import net.smileycorp.raids.common.world.WorldGenOutpost;
import net.smileycorp.raids.config.OutpostConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class RaidsBackportCompatCalls {
    private static final String CONVERTED_TAG = "MCAConvertedRaidBandit";
    private static final Set<ResourceLocation> CONVERTIBLE_RAIDERS = new HashSet<>(Arrays.asList(
            new ResourceLocation("raids", "pillager"),
            new ResourceLocation("minecraft", "vindication_illager")
    ));
    private static final Set<ResourceLocation> CONVERTIBLE_OUTPOST_ENTITIES = new HashSet<>(Arrays.asList(
            new ResourceLocation("raids", "pillager")
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
        if (!(entity instanceof EntityLiving) || entity instanceof EntityVillagerMCA || entity.ticksExisted < 1) {
            return;
        }

        EntityLiving source = (EntityLiving) entity;
        if (source.getEntityData().getBoolean(CONVERTED_TAG)) {
            return;
        }

        if (!isRaidOrPatrolRaider(source) && isExcessOutpostEntity(source)) {
            source.getEntityData().setBoolean(CONVERTED_TAG, true);
            source.setDead();
            return;
        }

        if (!isConvertibleBanditSource(source)) {
            return;
        }

        Raider sourceRaider = source.getCapability(RaidsContent.RAIDER, null);
        Raid raid = sourceRaider != null ? sourceRaider.getCurrentRaid() : null;
        int wave = sourceRaider != null ? sourceRaider.getWave() : 0;
        boolean wasLeader = sourceRaider != null && (sourceRaider.isPatrolLeader() || ItemStack.areItemStacksEqual(source.getItemStackFromSlot(EntityEquipmentSlot.HEAD), Constants.ominousBanner()));
        NBTTagCompound patrolData = sourceRaider != null && raid == null ? sourceRaider.writeNBT(new NBTTagCompound()) : null;

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

    private static boolean isConvertibleBanditSource(EntityLiving source) {
        ResourceLocation id = EntityList.getKey(source);
        if (id == null) {
            return false;
        }

        return CONVERTIBLE_RAIDERS.contains(id);
    }

    private static boolean isRaidOrPatrolRaider(EntityLiving source) {
        ResourceLocation id = EntityList.getKey(source);
        if (id == null || !CONVERTIBLE_RAIDERS.contains(id) || !source.hasCapability(RaidsContent.RAIDER, null)) {
            return false;
        }

        Raider raider = source.getCapability(RaidsContent.RAIDER, null);
        return raider != null && (raider.hasActiveRaid() || raider.isPatrolling());
    }

    private static boolean isExcessOutpostEntity(EntityLiving source) {
        ResourceLocation id = EntityList.getKey(source);
        int outpostEntities = getOutpostEntityCount(source);
        return id != null && CONVERTIBLE_OUTPOST_ENTITIES.contains(id) && outpostEntities > OutpostConfig.maxEntities;
    }

    private static int getOutpostEntityCount(EntityLiving source) {
        if (!(source.world instanceof WorldServer)) {
            return 0;
        }

        WorldGenOutpost.OutpostStart structure = WorldDataOutposts.getData((WorldServer) source.world).getStructureAt(source.getPosition());
        if (structure == null) {
            return -1;
        }

        return source.world.getEntitiesWithinAABB(EntityLivingBase.class, structure.getSpawnBox(), RaidsBackportCompatCalls::countsTowardOutpostCap).size();
    }

    private static boolean countsTowardOutpostCap(EntityLivingBase entity) {
        return entity instanceof EntityBanditMCA || OutpostConfig.isSpawnEntity(entity);
    }
}
