package mca.entity.ai;

import mca.core.MCA;
import mca.entity.EntityVillagerMCA;
import mca.core.minecraft.ProfessionsMCA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.List;

public class GuardEnemiesSensor {
    private final EntityVillagerMCA villager;
    private EntityLivingBase nearestEnemy;
    private int updateCooldown;
    
    public GuardEnemiesSensor(EntityVillagerMCA villager) {
        this.villager = villager;
    }
    
    public void tick() {
        if (updateCooldown > 0) {
            updateCooldown--;
            return;
        }
        
        updateCooldown = MCA.getConfig().guardSensorUpdateInterval;
        nearestEnemy = findNearestHostile();
    }
    
    @Nullable
    public EntityLivingBase getNearestEnemy() {
        return nearestEnemy;
    }
    
    public boolean hasEnemy() {
        return nearestEnemy != null && nearestEnemy.isEntityAlive();
    }
    
    @Nullable
    private EntityLivingBase findNearestHostile() {
        if (villager.world == null || villager.world.isRemote) {
            return null;
        }
        
        if (!isGuard(villager)) {
            return null;
        }
        
        double range = MCA.getConfig().guardPatrolRadius;
        AxisAlignedBB searchBox = villager.getEntityBoundingBox().grow(range, range / 2, range);
        
        List<EntityLivingBase> entities = villager.world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        
        EntityLivingBase result = null;
        int bestPriority = -1;
        double bestDistance = Double.MAX_VALUE;
        
        for (EntityLivingBase e : entities) {
            if (e == villager || !e.isEntityAlive() || !villager.canEntityBeSeen(e)) {
                continue;
            }
            
            int priority = getPriority(e, villager);
            if (priority < 0) {
                continue;
            }
            
            double distance = e.getDistanceSq(villager);
            
            if (priority > bestPriority || (priority == bestPriority && distance < bestDistance)) {
                bestPriority = priority;
                bestDistance = distance;
                result = e;
            }
        }
        
        return result;
    }
    
    private boolean isGuard(EntityVillagerMCA villager) {
        return villager.getProfessionForge() == ProfessionsMCA.guard;
    }
    
    private boolean isHostile(EntityLivingBase entity) {
        return getPriority(entity, null) >= 0;
    }
    
    private int getPriority(EntityLivingBase entity, EntityVillagerMCA guard) {
        if (entity instanceof EntityVillagerMCA) {
            EntityVillagerMCA otherVillager = (EntityVillagerMCA) entity;
            if (otherVillager.getProfessionForge() == ProfessionsMCA.bandit) {
                return MCA.getConfig().guardBanditPriority;
            }
            return -1;
        }
        
        if (guard != null && entity instanceof EntityMob) {
            EntityMob mob = (EntityMob) entity;
            if (mob.getAttackTarget() == guard) {
                return MCA.getConfig().guardAttackerPriority;
            }
        }
        
        String entityId = getEntityId(entity);
        if (entityId != null && MCA.getConfig().hasTargetPriority(entityId)) {
            return MCA.getConfig().getTargetPriority(entityId);
        }
        
        if (MCA.getConfig().guardsTargetMonsters && entity instanceof IMob) {
            return MCA.getConfig().guardUnknownMonsterPriority;
        }
        
        return -1;
    }
    
    @Nullable
    private String getEntityId(EntityLivingBase entity) {
        ResourceLocation rl = net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(entity.getClass()).getRegistryName();
        return rl != null ? rl.toString() : null;
    }
    
    public static boolean isPreferredTarget(EntityVillagerMCA guard, EntityLivingBase target) {
        if (guard == null || target == null) {
            return false;
        }
        
        if (guard.getProfessionForge() != ProfessionsMCA.guard) {
            return false;
        }
        
        GuardEnemiesSensor sensor = guard.getGuardSensor();
        if (sensor == null) {
            return false;
        }
        
        EntityLivingBase nearest = sensor.getNearestEnemy();
        return nearest == target;
    }
}
